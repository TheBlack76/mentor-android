package com.mentor.application.views.comman.dialgofragments

import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.R
import com.mentor.application.databinding.DialogDeleteBinding
import com.mentor.application.databinding.ToolbarDialogFragmentsBinding
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.OnBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DeleteAccountDialogFragment :
    BaseDialogFragment<DialogDeleteBinding>(DialogDeleteBinding::inflate),
    OnClickListener {

    private val mViewModel: OnBoardingViewModel by viewModels()


    override fun init() {

        // Set click listener
        binding.ivCancel.setOnClickListener(this)
        binding.tvNot.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)

    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onLogout().observe(this, Observer {
            showMessage(null, getString(R.string.st_deleted_successfully), true)
            navigateToMainActivity()
        })
    }

    override val isFullScreenDialog: Boolean
        get() = false


    override val toolbar: ToolbarDialogFragmentsBinding?
        get() = null

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivCancel -> {
                dismiss()
            }

            R.id.ivCancel, R.id.tvNot -> {
                dismiss()
            }

            R.id.btnSubmit -> {
                mViewModel.deleteAccount()
            }
        }
    }
}