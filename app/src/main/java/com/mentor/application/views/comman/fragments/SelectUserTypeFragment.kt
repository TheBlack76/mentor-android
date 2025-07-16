package com.mentor.application.views.comman.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import com.mentor.application.R
import com.mentor.application.databinding.FragmentUserTypeBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.utils.ApplicationGlobal
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.HomeActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SelectUserTypeFragment :
    BaseFragment<FragmentUserTypeBinding>(FragmentUserTypeBinding::inflate),
    OnClickListener {

    override val toolbar: ToolbarBinding?
        get() = null

    override fun init(savedInstanceState: Bundle?) {

        val text = getString(R.string.st_how_would_you_like_to_continue)
        val spannableString = SpannableString(text)

        val lastWordStartIndex = text.lastIndexOf(" ") + 1

        spannableString.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            ), // Replace Color.RED with your desired color
            lastWordStartIndex,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvHeader.text = spannableString
        ApplicationGlobal.mUserType=ApplicationGlobal.CUSTOMER

        //Set click listener
        binding.btnSubmit.setOnClickListener(this)
        binding.ivCustomer.setOnClickListener(this)
        binding.ivProfessional.setOnClickListener(this)
        binding.tvSkip.setOnClickListener(this)

    }


    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSubmit -> {
                (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                    fragment = LoginFragment(),
                    containerViewId = R.id.flFragContainerMain,
                    enterAnimation = R.animator.slide_right_in_fade_in,
                    exitAnimation = R.animator.scale_fade_out,
                    popExitAnimation = R.animator.slide_right_out_fade_out
                )
            }

            R.id.ivCustomer -> {
                binding.tvSkip.visibility=View.VISIBLE
                ApplicationGlobal.mUserType=ApplicationGlobal.CUSTOMER
                binding.cbCustomer.setImageResource(R.drawable.ic_checkmark)
                binding.cbProfessional.setImageResource(R.drawable.ic_un_checkmark)

            }

            R.id.ivProfessional -> {
                binding.tvSkip.visibility=View.GONE
                ApplicationGlobal.mUserType=ApplicationGlobal.PROFESSIONAL
                binding.cbCustomer.setImageResource(R.drawable.ic_un_checkmark)
                binding.cbProfessional.setImageResource(R.drawable.ic_checkmark)
            }
            R.id.tvSkip -> {
                requireContext().startActivity(
                    Intent(requireContext(), HomeActivity::class.java)
                )
                (activity as BaseAppCompactActivity<*>).finish()
            }
        }
    }

}
