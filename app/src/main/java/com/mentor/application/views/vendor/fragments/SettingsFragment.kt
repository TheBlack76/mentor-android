package com.mentor.application.views.vendor.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.mentor.application.R
import com.mentor.application.databinding.FragmentSettingsBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.networkrequests.WebConstants
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.comman.dialgofragments.DeleteAccountDialogFragment
import com.mentor.application.views.comman.dialgofragments.LogoutDialogFragment
import com.mentor.application.views.comman.dialgofragments.WebViewDialogFragment
import com.mentor.application.views.customer.fragment.ContactUsFragment
import com.mentor.application.views.customer.fragment.EditProfileFragment
import com.mentor.application.views.customer.fragment.HomeFragment.Companion.INTENT_HOME
import com.mentor.application.views.vendor.fragments.EnterDetailFragment.Companion.BUNDLE_VIEW_TYPE_EDIT
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingsFragment :
    BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate), OnClickListener {

    override val toolbar: ToolbarBinding?
        get() = null

    override fun init(savedInstanceState: Bundle?) {
        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_settings)
        binding.appBarLayout.toolbar.navigationIcon = null

        // Initialize receiver
        requireContext()
            .registerReceiver(
                mGetUpdateDataBroadcastReceiver,
                IntentFilter(INTENT_HOME), Context.RECEIVER_EXPORTED
            )

        // Set  click listener
        binding.tvEditProfile.setOnClickListener(this)
        binding.tvContactUs.setOnClickListener(this)
        binding.tvLogout.setOnClickListener(this)
        binding.tvDeleteAccount.setOnClickListener(this)
        binding.tvPersonalisation.setOnClickListener(this)
        binding.tvAddAvailability.setOnClickListener(this)
        binding.tvAccountDetail.setOnClickListener(this)
        binding.tvPolicies.setOnClickListener(this)
        binding.tvTermsCondition.setOnClickListener(this)

        checkDetailSetup()
    }

    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {

    }

    private fun checkDetailSetup() {
        // Helper function to update TextView properties
        fun updateTextViewState(
            textView: AppCompatTextView,
            isError: Boolean,
            errorColor: Int,
            normalColor: Int
        ) {
            val color = if (isError) errorColor else normalColor
            textView.setTextColor(ContextCompat.getColorStateList(requireContext(), color))

            // Update only the start drawable
            val drawables = textView.compoundDrawablesRelative
            val startDrawable = drawables[0]

            startDrawable?.let {
                val wrappedDrawable = DrawableCompat.wrap(it)
                DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(requireContext(), color))
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    wrappedDrawable, // Apply tint only to drawableStart
                    drawables[1],    // drawableTop (unchanged)
                    drawables[2],    // drawableEnd (unchanged)
                    drawables[3]     // drawableBottom (unchanged)
                )
            }
        }

        val errorColor = R.color.colorRed
        val normalColor = R.color.colorPrimaryText

        // Check and update each TextView
        updateTextViewState(
            binding.tvPersonalisation,
            mUserPrefsManager.loginUser?.isRegister == false,
            errorColor,
            normalColor
        )
        updateTextViewState(
            binding.tvAccountDetail,
            mUserPrefsManager.loginUser?.isBankAccount == false,
            errorColor,
            normalColor
        )
        updateTextViewState(
            binding.tvAddAvailability,
            mUserPrefsManager.loginUser?.isAvailability == false,
            errorColor,
            normalColor
        )
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvEditProfile -> {
                doTransaction(EditProfileFragment())
            }

            R.id.tvPolicies -> {
                WebViewDialogFragment.newInstance(getString(R.string.st_privacy_policies),
                    WebConstants.ACTION_PRIVACY_POLICY).show(childFragmentManager,"")
            }

            R.id.tvTermsCondition -> {
                WebViewDialogFragment.newInstance(getString(R.string.st_terms_amp_conditions),
                    WebConstants.ACTION_TERMS_AND_CONDITIONS).show(childFragmentManager,"")
            }

            R.id.tvPersonalisation -> {
                doTransaction(EnterDetailFragment.newInstance(BUNDLE_VIEW_TYPE_EDIT))
            }

            R.id.tvContactUs -> {
                doTransaction(ContactUsFragment())
            }

            R.id.tvAddAvailability -> {
                doTransaction(MyAvailabilityFragment())
            }

            R.id.tvAccountDetail -> {
                doTransaction(AccountDetailSetupFragment.newInstance(AccountDetailSetupFragment.BUNDLE_VIEW_TYPE_EDIT))

            }

            R.id.tvLogout -> {
                LogoutDialogFragment().show(childFragmentManager, "")
            }

            R.id.tvDeleteAccount -> {
                DeleteAccountDialogFragment().show(childFragmentManager, "")
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

    private val mGetUpdateDataBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, p1: Intent?) {
            context?.let {
                try {
                    checkDetailSetup()
                } catch (e: Exception) {
                    println(e)

                }
            }
        }
    }

}
