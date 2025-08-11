package com.mentor.application.views.customer.dialogFragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.BuildConfig
import com.mentor.application.R
import com.mentor.application.databinding.DialogInstantBookingProcessingBinding
import com.mentor.application.databinding.ToolbarDialogFragmentsBinding
import com.mentor.application.repository.models.Booking
import com.mentor.application.repository.models.BookingOffer
import com.mentor.application.views.comman.dialgofragments.BaseDialogFragment
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.customer.InstantBookingViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.VideoSessionActivity
import com.mentor.application.views.comman.activities.VideoSessionActivity.Companion.BOOKING_SESSION
import com.mentor.application.views.comman.activities.VideoSessionActivity.Companion.BOOKING_TYPE_INSTANT
import com.mentor.application.views.comman.activities.VideoSessionActivity.Companion.BUNDLE_BOOKING
import com.mentor.application.views.customer.adapters.VendorInstantRequestAdapter
import com.mentor.application.views.customer.interfaces.BookingOffersInterface
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.trendy.app.utils.DialogUtils
import com.trendy.app.utils.DialogUtils.CancelBookingDialogInterface
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class InstantBookingProcessDialogFragment :
    BaseDialogFragment<DialogInstantBookingProcessingBinding>(DialogInstantBookingProcessingBinding::inflate),
    OnClickListener, BookingOffersInterface {

    companion object {
        const val BUNDLE_BOOKING_ID = "bookingId"
        const val BUNDLE_PROFESSIONAL_ID = "professionalId"
        fun newInstance(
            bookingId: String, professionalId: String
        ): InstantBookingProcessDialogFragment {
            val args = Bundle()
            val fragment = InstantBookingProcessDialogFragment()
            args.putString(BUNDLE_BOOKING_ID, bookingId)
            args.putString(BUNDLE_PROFESSIONAL_ID, professionalId)
            fragment.arguments = args
            return fragment
        }
    }

    private val mViewModel: InstantBookingViewModel by viewModels()

    @Inject
    lateinit var mRequestAdapter: VendorInstantRequestAdapter

    private lateinit var paymentSheet: PaymentSheet

    private var bookingId = ""
    private var professionalId = ""

    override fun init() {
        isCancelable = false

        // Get arguments
        bookingId = arguments?.getString(BUNDLE_BOOKING_ID) ?: ""
        professionalId = arguments?.getString(BUNDLE_PROFESSIONAL_ID) ?: ""

        // Connect socket
        mViewModel.setUpChatSocket(bookingId)

        // Set adapter
        binding.recyclerView.adapter = mRequestAdapter

        // Set click listener
        binding.tvCancel.setOnClickListener(this)
        binding.tvGoBack.setOnClickListener(this)
        binding.btnSearchAgain.setOnClickListener(this)
    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onGetNotRespond().observe(this, Observer {
            binding.clNoResponse.visibility = View.VISIBLE
        })
        mViewModel.onGetNotRespondByProfessional().observe(this, Observer {
            binding.clNoResponse.visibility = View.VISIBLE
        })

        mViewModel.onGetBookingOffers().observe(this, Observer {
            bookingVisible()
            mRequestAdapter.updateData(it)
        })

        mViewModel.onGetBookingPayment().observe(this, Observer {
            initStripe(it.payment.customerId, it.payment.ephemeralKey, it.payment.paymentIntent)
        })

        mViewModel.onGetAcceptResponse().observe(this, Observer {
            dismiss()

            val booking = Booking(
                _id = it.updatedBooking._id,
                professionalName = it.updatedBooking.customerId.fullName,
            )

            startActivity(
                Intent(requireContext(), VideoSessionActivity::class.java).putExtra(
                    BUNDLE_BOOKING, booking

                ).putExtra(
                    VideoSessionActivity.BOOKING_TYPE, BOOKING_TYPE_INSTANT
                ).putExtra(BOOKING_SESSION, it.session)
            )
        })

    }

    override val isFullScreenDialog: Boolean
        get() = true


    override val toolbar: ToolbarDialogFragmentsBinding?
        get() = null

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.tvCancel -> {
                // Api call
                DialogUtils.cancelBookingRequestDialog(requireContext(),
                    layoutInflater = layoutInflater,
                    callBack = object : DialogUtils.CancelBookingRequestDialogInterface {
                        override fun onCancel() {
                            mMyCustomLoader.showProgressDialog()
                            mViewModel.cancelRequest(bookingId)
                            Handler(Looper.getMainLooper()).postDelayed({
                                dismiss()
                                mMyCustomLoader.dismissProgressDialog()
                            }, 1000)
                        }


                    })

            }

            R.id.btnSearchAgain -> {
                mViewModel.requestAgain(bookingId)
                binding.clNoResponse.visibility = View.GONE
            }

            R.id.tvGoBack -> {
                (activity as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun bookingVisible() {
        if (!binding.clRequestView.isVisible) {
            val slideUp: Animation = AnimationUtils.loadAnimation(
                requireContext(), R.anim.smooth_slide_fold_down
            )
            binding.clRequestView.visibility = View.VISIBLE
            binding.clRequestView.startAnimation(slideUp)
        }
    }

    private fun bookingHide() {
        if (binding.clRequestView.isVisible) {
            val slideUp: Animation = AnimationUtils.loadAnimation(
                requireContext(), R.anim.smooth_sllide_fold_up
            )
            binding.clRequestView.visibility = View.GONE
            binding.clRequestView.startAnimation(slideUp)

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        PaymentConfiguration.init(
            requireContext(), BuildConfig.STRIPE_LIVE_KEY
        )
        paymentSheet = PaymentSheet(this) { result ->
            onPaymentSheetResult(result)

        }
    }

    private fun initStripe(
        customerId: String, ephemeralKeySecret: String, paymentIntentClientSecret: String
    ) {

        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret, PaymentSheet.Configuration(
                merchantDisplayName = getString(R.string.app_name),
                primaryButtonColor = ContextCompat.getColorStateList(
                    requireContext(), R.color.colorPrimary
                ),
                customer = PaymentSheet.CustomerConfiguration(
                    id = customerId,
                    ephemeralKeySecret = ephemeralKeySecret,
                ),
                allowsDelayedPaymentMethods = false

            )
        )
    }


    private fun onPaymentSheetResult(
        paymentSheetResult: PaymentSheetResult
    ) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                Log.e("result", "onPaymentSheetResult$paymentSheetResult")
            }

            is PaymentSheetResult.Failed -> {
                Log.e("result", "Got error: ${paymentSheetResult.error}")
            }

            is PaymentSheetResult.Completed -> {

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewModel.disconnectChatSocket()
    }

    override fun onAccept(bookingOffer: BookingOffer) {
        mViewModel.acceptOffer(bookingId, bookingOffer.professionalId)

    }

    override fun onReject(bookingOffer: BookingOffer) {
        mViewModel.rejectOffer(bookingId, bookingOffer.professionalId)
        if (mRequestAdapter.onRemove(bookingOffer.professionalId).isEmpty()) {
            bookingHide()
        }
    }

}