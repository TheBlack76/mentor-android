package com.mentor.application.views.comman.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Annotation
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.TypefaceSpan
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.app.glambar.repository.models.post.PostFacebookLogin
import com.app.glambar.repository.models.post.PostGoogleLogin
import com.mentor.application.R
import com.mentor.application.databinding.FragmentLoginBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.OnBoardingViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.HomeActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.comman.base.BaseSocialLoginFragment
import com.mentor.application.views.customer.fragment.OtpVerificationFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginFragment :
    BaseSocialLoginFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate),
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

        urlLink()

        // Set onClick listener
        binding.btnSubmit.setOnClickListener(this)
        binding.btnGoogle.setOnClickListener(this)


    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onSignUpSuccess().observe(this, Observer {
            doTransaction(OtpVerificationFragment())

        })

        mViewModel.mSocialLoginSuccess().observe(this, Observer {
            if (mUserPrefsManager.loginUser?.isVerified == false) {
                doTransaction(PhoneNumberFragment())
            } else {
                requireContext().startActivity(
                    Intent(requireContext(), HomeActivity::class.java)
                )
                (activity as BaseAppCompactActivity<*>).finish()

            }
        })
    }

    override fun onSuccessfullFbLogin(postFacebookLogin: PostFacebookLogin) {

    }

    override fun onSuccessfullGoogleLogin(postGoogleLogin: PostGoogleLogin) {

        // Api call
        mViewModel.socialLogin(
            postGoogleLogin.name, postGoogleLogin.email,postGoogleLogin.image, postGoogleLogin.google_id
        )

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
                // Api call
                mViewModel.login(
                    binding.tvCountryCode.selectedCountryCodeWithPlus.toString().trim(),
                    binding.etNumber.text.toString().trim(),
                )

            }

            R.id.btnGoogle -> {
                doGooglePlusLogin()
            }

        }
    }

    private fun doTransaction(fragment: Fragment) {
        (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
            fragment = fragment,
            containerViewId = R.id.flFragContainerMain,
            enterAnimation = R.animator.slide_right_in_fade_in,
            exitAnimation = R.animator.scale_fade_out,
            popExitAnimation = R.animator.slide_right_out_fade_out
        )
    }

    private fun urlLink() {
        val spannableString = getText(R.string.st_dont_have_an_account) as SpannedString
        val spannable = SpannableStringBuilder(spannableString)
        val annotationList =
            spannableString.getSpans(0, spannableString.length, Annotation::class.java)

        for (annotation in annotationList) {
            val startSpan = spannable.getSpanStart(annotation)
            val endSpan = spannable.getSpanEnd(annotation)

            // Define your ClickableSpan
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    if ("0" == annotation.value) {
                        (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                            fragment = SignUpFragment(),
                            containerViewId = R.id.flFragContainerMain,
                            enterAnimation = R.animator.slide_right_in_fade_in,
                            exitAnimation = R.animator.scale_fade_out,
                            popExitAnimation = R.animator.slide_right_out_fade_out
                        )
                    }
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    // Set the color of the clickable text here
                    if (annotation.value == "0") {
                        ds.color = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                    }
                }
            }

            // Define your custom TypefaceSpan
            val typeface = ResourcesCompat.getFont(
                requireContext(),
                R.font.font_roboto_medium
            ) // Replace with your font file
            val typefaceSpan = if (typeface != null) {
                com.mentor.application.views.comman.utils.CustomTypefaceSpan(typeface)
            } else {
                TypefaceSpan("serif") // Fallback option
            }

            // Apply the spans
            spannable.setSpan(clickableSpan, startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(typefaceSpan, startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.tvSignUpTagline.text = spannable
        binding.tvSignUpTagline.movementMethod = LinkMovementMethod.getInstance()
    }

}
