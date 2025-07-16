package com.mentor.application.views.customer.dialogFragments

import android.view.View
import android.view.View.OnClickListener
import com.mentor.application.R
import com.mentor.application.databinding.DialogContactRequestBinding
import com.mentor.application.databinding.ToolbarDialogFragmentsBinding
import com.mentor.application.views.comman.dialgofragments.BaseDialogFragment
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity


class ContactRequestDialogFragment :
    BaseDialogFragment<DialogContactRequestBinding>(DialogContactRequestBinding::inflate),
    OnClickListener {

    override fun init() {
        isCancelable=false

        // Set click listener
        binding.btnSubmit.setOnClickListener(this)

    }

    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {
        // Set click listener
        binding.ivCancel.setOnClickListener(this)
    }

    override val isFullScreenDialog: Boolean
        get() = false


    override val toolbar: ToolbarDialogFragmentsBinding?
        get() = null

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivCancel -> {
                (activity as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
            }
            R.id.btnSubmit -> {
                (activity as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}