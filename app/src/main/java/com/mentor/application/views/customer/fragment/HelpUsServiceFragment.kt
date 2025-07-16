package com.mentor.application.views.customer.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mentor.application.BuildConfig
import com.mentor.application.R
import com.mentor.application.databinding.BottomsheetConfirmBookingBinding
import com.mentor.application.databinding.FragmentHelpUsServiceBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.CreateBookingData
import com.mentor.application.repository.models.TimeSlot
import com.mentor.application.utils.Constants
import com.mentor.application.utils.Constants.DATE_DISPLAY_TIME
import com.mentor.application.utils.Constants.DATE_FORMAT_DISPLAY
import com.mentor.application.utils.Constants.DATE_SERVER_TIME
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.customer.ServicesViewModel
import com.mentor.application.views.comman.dialgofragments.LoginDialogFragment
import com.mentor.application.views.customer.adapters.HelpUsQuestionAdapter
import com.mentor.application.views.customer.dialogFragments.PaymentSuccessFullDialogFragment
import com.mentor.application.views.customer.fragment.BookNowFragment.Companion.professionId
import com.mentor.application.views.customer.fragment.BookNowFragment.Companion.professionalId
import com.mentor.application.views.customer.fragment.BookNowFragment.Companion.subProfessionId
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HelpUsServiceFragment :
    BaseFragment<FragmentHelpUsServiceBinding>(FragmentHelpUsServiceBinding::inflate),
    OnClickListener {

    companion object {
        const val BUNDLE_BOOKING_DATE = "bookingDate"
        const val BUNDLE_BOOKING_SLOT = "bookingSlot"

        fun newInstance(date: String, slot: TimeSlot): HelpUsServiceFragment {
            val args = Bundle()
            val fragment = HelpUsServiceFragment()
            args.putString(BUNDLE_BOOKING_DATE, date)
            args.putParcelable(BUNDLE_BOOKING_SLOT, slot)
            fragment.arguments = args
            return fragment
        }
    }

    private val mViewModel: ServicesViewModel by viewModels()
    private lateinit var paymentSheet: PaymentSheet

    @Inject
    lateinit var mHelpUsQuestionAdapter: HelpUsQuestionAdapter

    private var mBookingDate = ""
    private var mBookingSlot = TimeSlot()

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun init(savedInstanceState: Bundle?) {

        // Get arguments
        arguments?.let {
            mBookingDate = it.getString(BUNDLE_BOOKING_DATE) ?: ""
            mBookingSlot = it.getParcelable(BUNDLE_BOOKING_SLOT) ?: TimeSlot()
        }

        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text =
            getString(R.string.st_help_us_tailor_your_service)

        // Set adapter
        binding.recyclerView.adapter = mHelpUsQuestionAdapter

        // Set click listener
        binding.btnSubmit.setOnClickListener(this)

        // Api call
        mViewModel.getQuestions(professionId)


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        PaymentConfiguration.init(
            requireContext(),
            BuildConfig.STRIPE_LIVE_KEY
        )
        paymentSheet = PaymentSheet(this) { result ->
            onPaymentSheetResult(result)

        }
    }

    private fun initStripe(
        bookingId: String,
        customerId: String,
        ephemeralKeySecret: String,
        paymentIntentClientSecret: String
    ) {

        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = getString(R.string.app_name),
                primaryButtonColor = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.colorPrimary
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
                PaymentSuccessFullDialogFragment().show(childFragmentManager, "")
            }
        }
    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onBookingConfirm().observe(this, Observer {
            PaymentSuccessFullDialogFragment().show(childFragmentManager, "")
        })

        mViewModel.onBookingCreated().observe(this, Observer {
            showFilterOptionsBottomSheet(it.data)
        })

        mViewModel.onGetQuestions().observe(this, Observer {
            // Update data
            mHelpUsQuestionAdapter.updateData(it)
        })
        

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
                // Api call
                if (mUserPrefsManager.isLogin) {
                    mViewModel.createBooking(
                        mBookingDate,
                        mBookingSlot.startTime,
                        mBookingSlot.endTime,
                        professionalId,
                        professionId,
                        subProfessionId,
                        mHelpUsQuestionAdapter.getAnswerList()
                    )
                } else {
                    LoginDialogFragment().show(childFragmentManager, "")
                }

            }
        }
    }

    private fun showFilterOptionsBottomSheet(bookingData: CreateBookingData) {
        val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.TransparentDialog)
        val view = BottomsheetConfirmBookingBinding.inflate(layoutInflater)

        view.tvDate.text = GeneralFunctions
            .changeDateFormat(bookingData.date, Constants.DATE_FORMAT_SERVER, DATE_FORMAT_DISPLAY)
        view.tvTime.text = GeneralFunctions.changeDateFormat(
            bookingData.startTime, DATE_SERVER_TIME, DATE_DISPLAY_TIME
        )
        view.tvCategory.text = bookingData.professionId.profession
        view.tvSubCategory.text = bookingData.subProfessionId.subProfession
        "$${bookingData.totalAmount}".also { view.tvTotalPrice.text = it }

        view.ivCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        view.btnSubmit.setOnClickListener {
            // Api call
            initStripe(
                bookingData._id,
                bookingData.payment.customerId,
                bookingData.payment.ephemeralKey,
                bookingData.payment.paymentIntent

            )
//            mViewModel.confirmBooking(bookingData._id)
        }

        bottomSheetDialog.setContentView(view.root)
        bottomSheetDialog.show()
    }

}
