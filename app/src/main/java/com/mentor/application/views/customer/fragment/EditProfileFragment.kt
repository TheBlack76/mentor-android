package com.mentor.application.views.customer.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.R
import com.mentor.application.databinding.FragmentEditProfileBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.utils.AmazonS3.Companion.SERVER_CUSTOMER_PHOTOS
import com.mentor.application.utils.AmazonS3.Companion.SERVER_PROFESSIONAL_PHOTOS
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.OnBoardingViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.customer.fragment.ProfileFragment.Companion.INTENT_PROFILE
import com.swingby.app.views.fragments.base.BasePictureOptionsFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class EditProfileFragment :
    BasePictureOptionsFragment<FragmentEditProfileBinding>(FragmentEditProfileBinding::inflate),
    OnClickListener {

    private val mViewModel: OnBoardingViewModel by viewModels()
    private var mImage = ""

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun setData(savedInstanceState: Bundle?) {

        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_edit_profile)

        // Set click listener
        binding.btnSubmit.setOnClickListener(this)
        binding.ivEdit.setOnClickListener(this)

        // Set user Detail
        mUserPrefsManager.loginUser.let {
            if (it?.image?.contains(SERVER_CUSTOMER_PHOTOS) == true || it?.image?.contains(
                    SERVER_PROFESSIONAL_PHOTOS
                ) == true
            ) {
                binding.sdvProfileImage.setImageURI(GeneralFunctions.getImage(it?.image ?: ""))
            } else {
                binding.sdvProfileImage.setImageURI(it?.image ?: "")
            }
            binding.etName.setText(it?.fullName)
            binding.etEmail.setText(it?.email)
            binding.etNumber.setText(it?.mobileNumber)
            binding.tvCountryCode.setCountryForPhoneCode(it?.countryCode?.toInt() ?: 91)
            mImage = it?.image.toString()

        }

        if (mUserPrefsManager.loginUser?.isSocialLogin == true) {
            binding.etEmail.isEnabled = false
        }
    }


    override fun onGettingImageFile(file: File) {
        binding.sdvProfileImage.setImageURI(GeneralFunctions.getLocalImageFile(file))
        mImage = file.absolutePath
    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onProfileUpdate().observe(this, Observer {
            if (binding.etNumber.text.toString()
                    .trim() != mUserPrefsManager.loginUser?.mobileNumber
                || binding.tvCountryCode.selectedCountryCodeWithPlus!=mUserPrefsManager.loginUser?.countryCode
            ) {
                (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                    fragment = OtpVerificationFragment.newInstance(
                        binding.etNumber.text.toString().trim(),
                        binding.tvCountryCode.selectedCountryCodeWithPlus.toString().trim()

                    ),
                    containerViewId = R.id.flFragContainerMain,
                    enterAnimation = R.animator.slide_right_in_fade_in,
                    exitAnimation = R.animator.scale_fade_out,
                    popExitAnimation = R.animator.slide_right_out_fade_out
                )
            } else {
                showMessage(null, getString(R.string.st_profile_updated_successfully), true)
                (activityContext as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()

                // Send broadcast
                requireContext().sendBroadcast(Intent(INTENT_PROFILE))
            }
        })

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
                // Check if phone number has changed
                val isPhoneChanged = binding.etNumber.text.toString()
                    .trim() != mUserPrefsManager.loginUser?.mobileNumber

                // Determine country code and number only if the phone number has changed
                val countryCode =
                    if (isPhoneChanged) binding.tvCountryCode.selectedCountryCodeWithPlus.toString()
                        .trim() else null
                val number = if (isPhoneChanged) binding.etNumber.text.toString().trim() else null

                // Api call
                mViewModel.editProfile(
                    name = binding.etName.text.toString().trim(),
                    email = binding.etEmail.text.toString().trim(),
                    image = mImage,
                    countryCode = countryCode,
                    number = number
                )

            }

            R.id.ivEdit -> {
                showPictureOptionsBottomSheet(GeneralFunctions.getOutputDirectory(requireContext()).absolutePath)

            }
        }
    }

}
