package com.mentor.application.views.customer.fragment

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.databinding.FragmentImageBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.mentor.application.BuildConfig
import com.mentor.application.R
import com.mentor.application.databinding.FragmentInstantBookingDurationBinding
import com.mentor.application.repository.models.InstantBookingPrice
import com.mentor.application.repository.models.Profession
import com.mentor.application.repository.models.SubProfession
import com.mentor.application.repository.models.User
import com.mentor.application.repository.models.enumValues.JobRates
import com.mentor.application.viewmodels.customer.InstantBookingViewModel
import com.mentor.application.viewmodels.customer.ServicesViewModel
import com.mentor.application.views.comman.utils.CustomTypefaceSpan
import com.mentor.application.views.customer.dialogFragments.InstantBookingProcessDialogFragment
import com.mentor.application.views.customer.dialogFragments.NoVendorFoundDialogFragment
import com.mentor.application.views.customer.dialogFragments.PaymentSuccessFullDialogFragment
import com.mentor.application.views.customer.fragment.AvailableWorkersFragment.Companion
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InstantBookingDurationFragment :
    BaseFragment<FragmentInstantBookingDurationBinding>(FragmentInstantBookingDurationBinding::inflate),
    OnClickListener {

    companion object {
        const val BUNDLE_PROFESSION_ID = "professionId"
        const val BUNDLE_SUB_PROFESSION_ID = "subProfessionId"
        const val BUNDLE_PROFESSIONAL_DATA = "professionalData"
        const val BUNDLE_SERVICE_NAME = "serviceName"

        fun newInstance(
            profession: Profession,
            subProfession: SubProfession,
            professionalData: User = User(),
        ): InstantBookingDurationFragment {
            val args = Bundle()
            val fragment = InstantBookingDurationFragment()
            args.putParcelable(BUNDLE_PROFESSION_ID, profession)
            args.putParcelable(BUNDLE_SUB_PROFESSION_ID, subProfession)
            args.putParcelable(BUNDLE_PROFESSIONAL_DATA, professionalData)
            fragment.arguments = args
            return fragment
        }
    }

    private val mViewModel: InstantBookingViewModel by viewModels()
    private var profession = Profession()
    private var professionalData = User()
    private var subProfession = SubProfession()
    private var professionName = ""
    private var slotType = JobRates.HALF_HOURLY.value
    private var slotPrice = ""
    private var instantBookingPrice: InstantBookingPrice? = null
    private var mBookingId = ""


    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun init(savedInstanceState: Bundle?) {

        // get arguments
        arguments.let {
            professionName = it?.getString(BUNDLE_SERVICE_NAME) ?: ""
            profession = it?.getParcelable(BUNDLE_PROFESSION_ID) ?: Profession()
            subProfession = it?.getParcelable(BUNDLE_SUB_PROFESSION_ID) ?: SubProfession()
            professionalData = it?.getParcelable(BUNDLE_PROFESSIONAL_DATA) ?: User()
        }


        // Set toolbar title
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_requested_instant_booking)

        // Set data
        binding.sdvServiceImage.setImageURI(subProfession.image)
        binding.tvProfessionName.text=profession.profession
        binding.tvSubProfessionName.text=subProfession.subProfession

        // Set click listener
        binding.tv30Min.setOnClickListener(this)
        binding.tv60Min.setOnClickListener(this)
        binding.tv90Min.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)

        // Call pai
        mViewModel.getBookingPrices(profession._id,subProfession._id)


        // Spannable String
        setSpannableString()
    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onGetBookingPrice().observe(this, Observer {
            instantBookingPrice = it
            slotPrice = it?.halfHourly.toString()

            binding.tvRecommended.text=
                getString(R.string.st_recommended_fare, it.halfHourly)
            binding.etPrice.setText("${it.halfHourly}")
        })

        mViewModel.onGetNoWorkerAvailable().observe(this, Observer {
            binding.viewNoWorker.visibility = View.VISIBLE
            binding.tvNoData.visibility = View.VISIBLE
            binding.btnSubmit.visibility = View.GONE
        })

        mViewModel.onGetBookingData().observe(this, Observer {
//            // Api call
//            initStripe(
//                it._id,
//                it.payment.customerId,
//                it.payment.ephemeralKey,
//                it.payment.paymentIntent
//
//            )
//
            mBookingId = it._id

            InstantBookingProcessDialogFragment.newInstance(mBookingId,professionalData._id)
                .show(childFragmentManager, "")

        })

    }

    private fun handle30Min() {
        binding.tvRecommended.text=
            getString(R.string.st_recommended_fare, instantBookingPrice?.halfHourly)
        binding.etPrice.setText("${instantBookingPrice?.halfHourly}")
        slotPrice=instantBookingPrice?.halfHourly.toString()

        slotType = JobRates.HALF_HOURLY.value

        binding.tv30Min.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            R.color.colorPrimary
        )
        binding.tv30Min.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.colorWhite
            )
        )

        binding.tv60Min.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            R.color.colorPrimaryPeach
        )
        binding.tv60Min.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.colorPrimary
            )
        )

        binding.tv90Min.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            R.color.colorPrimaryPeach
        )
        binding.tv90Min.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.colorPrimary
            )
        )
    }

    private fun handle60Min() {

        binding.tvRecommended.text=
            getString(R.string.st_recommended_fare, instantBookingPrice?.hourly)
        binding.etPrice.setText("${instantBookingPrice?.hourly}")
        slotPrice=instantBookingPrice?.hourly.toString()

        slotType = JobRates.HOURLY.value


        binding.tv60Min.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            R.color.colorPrimary
        )

        binding.tv60Min.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.colorWhite
            )
        )

        binding.tv30Min.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            R.color.colorPrimaryPeach
        )

        binding.tv30Min.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.colorPrimary
            )
        )

        binding.tv90Min.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            R.color.colorPrimaryPeach
        )

        binding.tv90Min.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.colorPrimary
            )
        )
    }

    private fun handle90Min() {
        binding.tvRecommended.text=
            getString(R.string.st_recommended_fare, instantBookingPrice?.oneAndHalfHourly)
        binding.etPrice.setText("${instantBookingPrice?.oneAndHalfHourly}")
        slotPrice=instantBookingPrice?.oneAndHalfHourly.toString()

        slotType = JobRates.ONE_AND_HALF_HOURLY.value

        binding.tv90Min.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            R.color.colorPrimary
        )
        binding.tv90Min.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.colorWhite
            )
        )

        binding.tv30Min.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            R.color.colorPrimaryPeach
        )
        binding.tv30Min.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.colorPrimary
            )
        )

        binding.tv60Min.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            R.color.colorPrimaryPeach
        )

        binding.tv60Min.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.colorPrimary
            )
        )
    }

    private fun setSpannableString() {
        val fullText =
            "Note:- You must pay the maximum price for the service upfront, and any excess " +
                    "amount will be refunded once a service provider accepts the request."

        val spannableString = SpannableString(fullText)

        // Load custom fonts
        val noteTypeface = ResourcesCompat.getFont(requireContext(), R.font.font_roboto_bold)
        val otherTypeface = ResourcesCompat.getFont(requireContext(), R.font.font_roboto_regular)

        // Apply custom font to "Note:-"
        noteTypeface?.let {
            val noteSpan = CustomTypefaceSpan(it)
            spannableString.setSpan(noteSpan, 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // Apply different color to "Note:-"
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.colorPrimaryText)),
            0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply custom font to the rest of the text
        otherTypeface?.let {
            val otherSpan = CustomTypefaceSpan(it)
            spannableString.setSpan(
                otherSpan,
                5,
                fullText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Apply different color to the rest of the text
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.colorDisabledText)),
            5, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.tv30Min -> handle30Min()
            R.id.tv60Min -> handle60Min()
            R.id.tv90Min -> handle90Min()
            R.id.btnSubmit -> {
                // Api call
                mViewModel.createInstantBooking(
                    profession._id,
                    subProfession._id,
                    professionalData._id,
                    binding.etPrice.text.toString(),
                    slotType,
                    binding.etComments.text.toString()
                )
            }
        }
    }


}

