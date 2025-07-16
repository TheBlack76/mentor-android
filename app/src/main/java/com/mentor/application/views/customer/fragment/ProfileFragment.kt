package com.mentor.application.views.customer.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.R
import com.mentor.application.databinding.FragmentUserProfileBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.networkrequests.WebConstants
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.OnBoardingViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.comman.dialgofragments.DeleteAccountDialogFragment
import com.mentor.application.views.comman.dialgofragments.LoginDialogFragment
import com.mentor.application.views.comman.dialgofragments.LogoutDialogFragment
import com.mentor.application.views.comman.dialgofragments.WebViewDialogFragment
import com.mentor.application.views.customer.fragment.HomeFragment.Companion.INTENT_HOME
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class ProfileFragment :
    BaseFragment<FragmentUserProfileBinding>(FragmentUserProfileBinding::inflate), OnClickListener {

    companion object {
        const val INTENT_PROFILE = "profileIntent"
    }

    private val mViewModel: OnBoardingViewModel by viewModels()

    override val toolbar: ToolbarBinding?
        get() = null

    override fun init(savedInstanceState: Bundle?) {
        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_my_profile)
        binding.appBarLayout.toolbar.navigationIcon = null

        // Initialize receiver
        requireContext()
            .registerReceiver(
                mGetUpdateDataBroadcastReceiver,
                IntentFilter(INTENT_PROFILE), Context.RECEIVER_EXPORTED
            )

        // Set  click listener
        binding.tvEditProfile.setOnClickListener(this)
        binding.tvContactUs.setOnClickListener(this)
        binding.tvLogout.setOnClickListener(this)
        binding.tvDeleteAccount.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.tvTermsCondition.setOnClickListener(this)
        binding.tvPolicies.setOnClickListener(this)

        // Set user detail
        if (mUserPrefsManager.isLogin) {
            binding.btnLogin.visibility=View.GONE
            mUserPrefsManager.loginUser.let {
                binding.sdvProfile.setImageURI(it?.image?.let { it1 ->
                    GeneralFunctions.getUserImage(
                        it1
                    )
                })

                binding.tvName.text = it?.fullName
                (it?.countryCode + it?.mobileNumber).also { binding.tvNumber.text = it }
            }
        }else{
            binding.btnLogin.visibility=View.VISIBLE
        }

    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onProfileUpdate().observe(this, Observer {
            // Set user detail
            requireContext().sendBroadcast(Intent(INTENT_HOME))
            mUserPrefsManager.loginUser.let {
                // If the file exist with same name in local show from local else from server
                val mImageFile =
                    GeneralFunctions.getLocalMediaFile(
                        requireContext(),
                        File(mUserPrefsManager.loginUser?.image ?: "").name
                    )

                val path = if (mImageFile.exists()) {
                    GeneralFunctions.getLocalImageFile(mImageFile)
                } else {
                    GeneralFunctions.getUserImage(mUserPrefsManager.loginUser?.image ?: "")
                }
                binding.sdvProfile.setImageURI(path)

                binding.tvName.text = it?.fullName
                (it?.countryCode + it?.mobileNumber).also { binding.tvNumber.text = it }
            }
        })

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvEditProfile -> {
                if (mUserPrefsManager.isLogin) {
                    (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                        fragment = EditProfileFragment(),
                        containerViewId = R.id.flFragContainerMain,
                        enterAnimation = R.animator.slide_right_in_fade_in,
                        exitAnimation = R.animator.scale_fade_out,
                        popExitAnimation = R.animator.slide_right_out_fade_out
                    )
                } else {
                    LoginDialogFragment().show(childFragmentManager, "")
                }
            }

            R.id.tvContactUs -> {
                if (mUserPrefsManager.isLogin) {
                    (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                        fragment = ContactUsFragment(),
                        containerViewId = R.id.flFragContainerMain,
                        enterAnimation = R.animator.slide_right_in_fade_in,
                        exitAnimation = R.animator.scale_fade_out,
                        popExitAnimation = R.animator.slide_right_out_fade_out
                    )
                } else {
                    LoginDialogFragment().show(childFragmentManager, "")
                }
            }

            R.id.tvLogout -> {
                if (mUserPrefsManager.isLogin) {
                    LogoutDialogFragment().show(childFragmentManager, "")

                } else {
                    LoginDialogFragment().show(childFragmentManager, "")
                }
            }
            R.id.btnLogin -> {
                navigateToMainActivity()
            }

            R.id.tvPolicies -> {
                WebViewDialogFragment.newInstance(getString(R.string.st_privacy_policies),
                    WebConstants.ACTION_PRIVACY_POLICY).show(childFragmentManager,"")
            }

            R.id.tvTermsCondition -> {
                WebViewDialogFragment.newInstance(getString(R.string.st_terms_amp_conditions),
                    WebConstants.ACTION_TERMS_AND_CONDITIONS).show(childFragmentManager,"")
            }

            R.id.tvDeleteAccount -> {
                if (mUserPrefsManager.isLogin) {
                    DeleteAccountDialogFragment().show(childFragmentManager, "")
                } else {
                    LoginDialogFragment().show(childFragmentManager, "")
                }
            }
        }
    }

    private val mGetUpdateDataBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, p1: Intent?) {
            context?.let {
                try {
                    // Api call
                    mViewModel.getProfile()
                } catch (e: Exception) {
                    println(e)

                }

            }
        }
    }

}
