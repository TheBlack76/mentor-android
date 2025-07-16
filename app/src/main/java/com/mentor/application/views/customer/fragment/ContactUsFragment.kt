package com.mentor.application.views.customer.fragment

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.R
import com.mentor.application.databinding.FragmentContactUsBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.vendor.PersonalisationViewModel
import com.mentor.application.views.customer.dialogFragments.ContactRequestDialogFragment
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ContactUsFragment :
    BaseFragment<FragmentContactUsBinding>(FragmentContactUsBinding::inflate),
    OnClickListener {

    private val mViewModel: PersonalisationViewModel by viewModels()

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun init(savedInstanceState: Bundle?) {

        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_contact_us)

        // Set click listener
        binding.btnSubmit.setOnClickListener(this)

        // Set data
        binding.etName.setText(mUserPrefsManager.loginUser?.fullName)
        binding.etEmail.setText(mUserPrefsManager.loginUser?.email)

    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onDetailSubmitted().observe(this, Observer {
            ContactRequestDialogFragment().show(childFragmentManager,"")

        })

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
                // Api call
                mViewModel.contactUs(
                    binding.etName.text.toString().trim(),
                    binding.etEmail.text.toString().trim(),
                    binding.etDescription.text.toString().trim(),
                )


            }
        }
    }

}
