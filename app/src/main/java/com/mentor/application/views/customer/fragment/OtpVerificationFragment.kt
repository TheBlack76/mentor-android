package com.mentor.application.views.customer.fragment

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.R
import com.mentor.application.databinding.FragmentOtpVerificationBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.utils.ApplicationGlobal
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.OnBoardingViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.HomeActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.customer.fragment.ProfileFragment.Companion.INTENT_PROFILE
import com.mentor.application.views.vendor.fragments.AccountDetailSetupFragment
import com.mentor.application.views.vendor.fragments.AccountDetailSetupFragment.Companion.BUNDLE_VIEW_TYPE_CREATE
import com.mentor.application.views.vendor.fragments.EnterDetailFragment
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OtpVerificationFragment :
    BaseFragment<FragmentOtpVerificationBinding>(FragmentOtpVerificationBinding::inflate),
    OnClickListener {

    companion object {
        const val BUNDLE_PHONE_NUMBER = "phoneNumber"
        const val BUNDLE_COUNTRY_CODE = "countryCode"

        fun newInstance(number: String, countryCode: String): OtpVerificationFragment {
            val args = Bundle()
            val fragment = OtpVerificationFragment()
            args.putString(BUNDLE_PHONE_NUMBER, number)
            args.putString(BUNDLE_COUNTRY_CODE, countryCode)
            fragment.arguments = args
            return fragment
        }
    }


    private val mViewModel: OnBoardingViewModel by viewModels()

    private var otpText = ""
    private var phoneNumber = ""
    private var countryCode = ""

    lateinit var countDownTimer: CountDownTimer

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun init(savedInstanceState: Bundle?) {

        // Set toolbar
        binding.appBarLayout.toolbar.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), R.color.colorWhite
        )
        binding.appBarLayout.toolbar.setNavigationIcon(R.drawable.ic_black_back)

        // Get arguments
        phoneNumber = arguments?.getString(BUNDLE_PHONE_NUMBER) ?: ""
        countryCode = arguments?.getString(BUNDLE_COUNTRY_CODE) ?: ""

        otpView()

        // Start countdown
        countDown()

        // Set click listener
        binding.btnSubmit.setOnClickListener(this)
        binding.tvResend.setOnClickListener(this)

    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onSignUpSuccess().observe(this, Observer {

            // todo when verification from login and signup
            if (phoneNumber.isBlank()) {
                if (ApplicationGlobal.mUserType == ApplicationGlobal.PROFESSIONAL &&
                    mUserPrefsManager.loginUser?.isRegister == false
                ) {
                    (activityContext as BaseAppCompactActivity<*>).doFragmentTransaction(
                        fragment = EnterDetailFragment.newInstance(EnterDetailFragment.BUNDLE_VIEW_TYPE_CREATE),
                        containerViewId = R.id.flFragContainerMain,
                        enterAnimation = R.animator.slide_right_in_fade_in,
                        exitAnimation = R.animator.scale_fade_out,
                        popExitAnimation = R.animator.slide_right_out_fade_out
                    )
                }
//                else if (ApplicationGlobal.mUserType == ApplicationGlobal.PROFESSIONAL &&
//                    mUserPrefsManager.loginUser?.isBankAccount == false
//                ) {
//                    (activityContext as BaseAppCompactActivity<*>).doFragmentTransaction(
//                        fragment = AccountDetailSetupFragment.newInstance(BUNDLE_VIEW_TYPE_CREATE),
//                        containerViewId = R.id.flFragContainerMain,
//                        enterAnimation = R.animator.slide_right_in_fade_in,
//                        exitAnimation = R.animator.scale_fade_out,
//                        popExitAnimation = R.animator.slide_right_out_fade_out
//                    )
//                }
                else {
                    requireContext().startActivity(
                        Intent(requireContext(), HomeActivity::class.java)
                    )
                    (activity as BaseAppCompactActivity<*>).finish()
                }
            } else {
                // todo when verification from update profile
                showMessage(null, getString(R.string.st_profile_updated_successfully), true)
                (activityContext as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
                (activityContext as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()

                // Send broadcast
                requireContext().sendBroadcast(Intent(INTENT_PROFILE))
            }
        })

        mViewModel.onOtpResend().observe(this, Observer {
            countDown()
        })
    }

    private fun otpView() {
        // OtpView listener
        binding.otpView.requestFocus()
        showSoftKeyboard()

        binding.otpView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                text: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                otpText = text.toString()
                if (otpText.length == 4) {
                    hideSoftKeyboard()
                }

            }
        })

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
                // Api call
                mViewModel.verifyOtp(
                    otpText, if (phoneNumber.isNotBlank()) phoneNumber else null,
                    if (phoneNumber.isNotBlank()) countryCode else null
                )

            }

            R.id.tvResend -> {
                binding.otpView.setText("")
                otpText = ""

                // Api call
                mViewModel.resendOtp()

            }

        }
    }

    private fun countDown() {
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvAlreadyRegistered.visibility = View.VISIBLE
                binding.tvResend.isClickable = false
                binding.tvResend.isEnabled = false
                (" 0:" + (millisUntilFinished / 1000).toString()).also {
                    binding.tvResend.text = it
                }
                //here you can have your logic to set text to edittext
            }

            override fun onFinish() {
                binding.tvResend.isClickable = true
                binding.tvResend.isEnabled = true
                binding.tvResend.text = getString(R.string.st_resend)
                binding.tvAlreadyRegistered.visibility = View.GONE
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer.cancel()
    }

}
