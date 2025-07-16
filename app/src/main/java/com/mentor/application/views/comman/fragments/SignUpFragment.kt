package com.mentor.application.views.comman.fragments

import android.app.Activity
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.app.glambar.repository.models.post.PostGoogleLogin
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.mentor.application.R
import com.mentor.application.databinding.FragmentSignupBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.OnBoardingViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.HomeActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.customer.fragment.OtpVerificationFragment
import com.swingby.app.views.fragments.base.BasePictureOptionsFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class SignUpFragment :
    BasePictureOptionsFragment<FragmentSignupBinding>(FragmentSignupBinding::inflate),
    OnClickListener {

    private val mViewModel: OnBoardingViewModel by viewModels()

    private var mImage = ""

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun setData(savedInstanceState: Bundle?) {
        // Set toolbar
        binding.appBarLayout.toolbar.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), R.color.colorWhite
        )
        binding.appBarLayout.toolbar.setNavigationIcon(R.drawable.ic_black_back)

        urlLink()

        // Set click listener
        binding.btnSubmit.setOnClickListener(this)
        binding.btnGoogle.setOnClickListener(this)
        binding.sdvProfilePicture.setOnClickListener(this)
    }


    override fun onGettingImageFile(file: File) {
        binding.sdvProfilePicture.setImageURI(
            GeneralFunctions.getLocalImageFile(file)
        )
        mImage = file.absolutePath
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
                // APi call
                mViewModel.signup(
                    binding.etName.text.toString().trim(),
                    binding.etEmail.text.toString().trim(),
                    binding.tvCountryCode.selectedCountryCodeWithPlus.toString().trim(),
                    binding.etNumber.text.toString().trim(),
                    mImage
                )
            }

            R.id.btnGoogle -> {
                doGooglePlusLogin()
            }

            R.id.sdvProfilePicture -> {
                showPictureOptionsBottomSheet(GeneralFunctions.getOutputDirectory(requireContext()).absolutePath)


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
        val spannableString = getText(R.string.already_have_an_account) as SpannedString
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
                       (activityContext as BaseAppCompactActivity<*>).onBackPressed()
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

        binding.tvSignInTagline.text = spannable
        binding.tvSignInTagline.movementMethod = LinkMovementMethod.getInstance()
    }

    /**
     * Google Sign-In
     */
    private val mGoogleSignInClient: GoogleSignInClient? by lazy {
        // Configure sign-in to request the user's ID, email address, and basic
        // user. ID and basic user are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions
                .DEFAULT_SIGN_IN
        )
            .requestEmail()
            .build()

        // New Google SignIn Client
        GoogleSignIn.getClient(activityContext, gso)
    }

    private fun doGooglePlusLogin() {
        val signInIntent = mGoogleSignInClient?.signInIntent
        googleActivityResultLauncher.launch(signInIntent)
    }

    private var googleActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val googleSignInAccount = task.getResult(ApiException::class.java)
                // Create PojoRegister object out of response
                val pojoGoogleLogin = PostGoogleLogin(
                    googleSignInAccount?.photoUrl.toString(),
                    googleSignInAccount?.displayName ?: "",
                    googleSignInAccount?.id ?: "0",
                    googleSignInAccount?.email ?: ""

                )

                // Api call
                mViewModel.socialLogin(
                    pojoGoogleLogin.name, pojoGoogleLogin.email,pojoGoogleLogin.image, pojoGoogleLogin.google_id
                )
                mGoogleSignInClient?.signOut()

            } catch (e: ApiException) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                showMessage(resId = R.string.retrofit_failure)
            }

        }
    }


}
