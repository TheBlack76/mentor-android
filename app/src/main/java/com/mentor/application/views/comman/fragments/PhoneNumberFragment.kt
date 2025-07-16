package com.mentor.application.views.comman.fragments

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.R
import com.mentor.application.databinding.FragmentPhoneNumberBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.OnBoardingViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.customer.fragment.OtpVerificationFragment
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PhoneNumberFragment :
    BaseFragment<FragmentPhoneNumberBinding>(FragmentPhoneNumberBinding::inflate),
    OnClickListener {

    private val mViewModel: OnBoardingViewModel by viewModels()

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun init(savedInstanceState: Bundle?) {
        // Set toolbar
        binding.appBarLayout.toolbar.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), R.color.colorWhite
        )

        binding.appBarLayout.toolbar.setNavigationIcon(R.drawable.ic_black_back)

        // Set onClick listener
        binding.btnSubmit.setOnClickListener(this)


    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onSignUpSuccess().observe(this, Observer {
            (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                fragment = OtpVerificationFragment(),
                containerViewId = R.id.flFragContainerMain,
                enterAnimation = R.animator.slide_right_in_fade_in,
                exitAnimation = R.animator.scale_fade_out,
                popExitAnimation = R.animator.slide_right_out_fade_out
            )
        })
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
                // Api call
                mViewModel.registerNumber(
                    binding.tvCountryCode.selectedCountryCode.toString().trim(),
                    binding.etNumber.text.toString().trim(),
                )
            }

        }
    }

}
