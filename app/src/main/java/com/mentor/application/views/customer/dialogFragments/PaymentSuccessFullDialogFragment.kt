package com.mentor.application.views.customer.dialogFragments

import android.content.Intent
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.AnimationUtils
import com.mentor.application.R
import com.mentor.application.databinding.DialogPaymentSuccessfullBinding
import com.mentor.application.databinding.ToolbarDialogFragmentsBinding
import com.mentor.application.views.comman.dialgofragments.BaseDialogFragment
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.comman.activities.HomeActivity
import com.mentor.application.views.customer.fragment.BookNowFragment.Companion.professionalName
import com.mentor.application.views.customer.fragment.DashboardFragment.Companion.BUNDLE_TAB_BOOKINGS
import com.mentor.application.views.customer.fragment.DashboardFragment.Companion.BUNDLE_TAB_NUMBER


class PaymentSuccessFullDialogFragment :
    BaseDialogFragment<DialogPaymentSuccessfullBinding>(DialogPaymentSuccessfullBinding::inflate),
    OnClickListener {

    override fun init() {
        isCancelable = false

        // Set animation
        val animObjText = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        animObjText.duration = 2500
        animObjText.startOffset = 300
        binding.ivSuccessFull.startAnimation(animObjText)

        // Set animation
        val animObjText2 = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_text)
        animObjText.duration = 1000
        animObjText2.startOffset = 1200
        binding.tvPaymentSuccessFull.startAnimation(animObjText2)
        binding.tvTagline.startAnimation(animObjText2)
        binding.btnSubmit.startAnimation(animObjText2)

        // Set click listener
        binding.btnSubmit.setOnClickListener(this)

        binding.tvTagline.text =
            getString(
                R.string.st_your_booking_is_confirmed_with_enjoy_your_service,
                professionalName
            )

    }

    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {

    }

    override val isFullScreenDialog: Boolean
        get() = true


    override val toolbar: ToolbarDialogFragmentsBinding?
        get() = null

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnSubmit -> {
                val intent = Intent(requireContext(), HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra(BUNDLE_TAB_NUMBER,BUNDLE_TAB_BOOKINGS)
                startActivity(intent)
            }
        }
    }
}