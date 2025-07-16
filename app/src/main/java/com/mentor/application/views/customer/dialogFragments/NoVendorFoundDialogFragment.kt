package com.mentor.application.views.customer.dialogFragments

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.mentor.application.R
import com.mentor.application.databinding.DialogContactRequestBinding
import com.mentor.application.databinding.DialogNoProviderFoundBinding
import com.mentor.application.databinding.ToolbarDialogFragmentsBinding
import com.mentor.application.views.comman.dialgofragments.BaseDialogFragment
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import org.jsoup.Connection.Base


class NoVendorFoundDialogFragment :
    BaseDialogFragment<DialogNoProviderFoundBinding>(DialogNoProviderFoundBinding::inflate),
    OnClickListener {

    companion object {
        const val BUNDLE_BY_PROFESSIONAL = "byProfessional"

        fun newInstance(isByProfessional: Boolean) : NoVendorFoundDialogFragment {
            val args = Bundle()
            val fragment = NoVendorFoundDialogFragment()
            fragment.arguments = args
            args.putBoolean(BUNDLE_BY_PROFESSIONAL, isByProfessional)
            return fragment
        }
    }

    override fun init() {
        // Set click listener
        binding.tvGoBack.setOnClickListener(this)
        binding.btnSearchAgain.setOnClickListener(this)

        if (arguments?.getBoolean(BUNDLE_BY_PROFESSIONAL) == true) {
            binding.tvTitle.text = getString(R.string.st_provider_not_available)
            binding.tvTagline.text =
                "The selected provider didn't pick up your request. Please go back and refine your search."

        }

    }

    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {

    }

    override val isFullScreenDialog: Boolean
        get() = false


    override val toolbar: ToolbarDialogFragmentsBinding?
        get() = null

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSearchAgain -> {
                dismiss()
            }

            R.id.tvGoBack -> {
                (activity as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
                (activity as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}