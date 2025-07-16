package com.mentor.application.views.comman.dialgofragments

import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.R
import com.mentor.application.databinding.DialogLogoutBinding
import com.mentor.application.databinding.ToolbarDialogFragmentsBinding
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginDialogFragment :
    BaseDialogFragment<DialogLogoutBinding>(DialogLogoutBinding::inflate),
    OnClickListener {

    private val mViewModel: OnBoardingViewModel by viewModels()

    override fun init() {
        // Set click listener
        binding.ivCancel.setOnClickListener(this)
        binding.tvNot.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)

        // Set text
        binding.ivLogo.setImageResource(R.drawable.ic_login)
        binding.tvTagline.text= getString(R.string.st_please_log_in_to_unlock_and_explore_more)
        binding.tvTitle.text= getString(R.string.st_login_required)
        binding.btnSubmit.text=getString(R.string.action_okay)
    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onLogout().observe(this, Observer {
            showMessage(null, getString(R.string.st_logout_successfully),true)
            navigateToMainActivity()
        })
    }

    override val isFullScreenDialog: Boolean
        get() = false

    override val toolbar: ToolbarDialogFragmentsBinding?
        get() = null

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivCancel, R.id.tvNot -> {
                dismiss()
            }

            R.id.btnSubmit -> {
               navigateToMainActivity()
            }
        }
    }
}