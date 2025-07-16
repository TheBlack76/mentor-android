package com.mentor.application.views.vendor.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.BuildConfig
import com.mentor.application.R
import com.mentor.application.databinding.FragmentAccountDetailConnectBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.vendor.PersonalisationViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.HomeActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.customer.fragment.ProfileFragment.Companion.INTENT_PROFILE
import com.stripe.android.Stripe
import com.stripe.android.model.BankAccountTokenParams
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AccountDetailSetupFragment :
    BaseFragment<FragmentAccountDetailConnectBinding>(FragmentAccountDetailConnectBinding::inflate),
    OnClickListener {

    companion object {
        const val BUNDLE_VIEW_TYPE = "viewType"
        const val BUNDLE_VIEW_TYPE_CREATE = 0
        const val BUNDLE_VIEW_TYPE_EDIT = 1

        fun newInstance(viewType: Int): AccountDetailSetupFragment {
            val args = Bundle()
            val fragment = AccountDetailSetupFragment()
            args.putInt(BUNDLE_VIEW_TYPE, viewType)
            fragment.arguments = args
            return fragment
        }
    }

    private val mViewModel: PersonalisationViewModel by viewModels()

    private var country = "US"
    private var currency = "USD"
    private var mViewType = BUNDLE_VIEW_TYPE_CREATE

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun init(savedInstanceState: Bundle?) {
        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_account_detail)

        // Get arguments
        mViewType = arguments?.getInt(BUNDLE_VIEW_TYPE) ?: BUNDLE_VIEW_TYPE_CREATE

        if (mViewType == BUNDLE_VIEW_TYPE_CREATE) {
            binding.appBarLayout.toolbar.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(), R.color.colorWhite
            )
            binding.appBarLayout.toolbar.setNavigationIcon(R.drawable.ic_black_back)

            binding.appBarLayout.toolbar.setNavigationOnClickListener {
                (activity as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
                (activity as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
            }

        } else {
            binding.appBarLayout.toolbar.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(), R.color.colorPrimary
            )

            binding.tvSkip.visibility = View.GONE
//            binding.tvHeader.visibility = View.GONE
//            binding.tvTagline.visibility = View.GONE
        }

        // Set  click listener
        binding.btnSubmit.setOnClickListener(this)
        binding.tvSkip.setOnClickListener(this)

        initSpinner()

        // Api call
        if (mUserPrefsManager.loginUser?.isBankAccount == true){
            mViewModel.getAccountDetail()
        }
    }

    override val viewModel: BaseViewModel?
        get() = mViewModel

    override fun observeProperties() {
        mViewModel.onDetailSubmitted().observe(this, Observer {
            if (mViewType == BUNDLE_VIEW_TYPE_CREATE) {
                requireContext().startActivity(
                    Intent(requireContext(), HomeActivity::class.java)
                )
                (activity as BaseAppCompactActivity<*>).finish()

            } else {
                showMessage(null, getString(R.string.st_detail_submitted_successfully), true)
                (activityContext as BaseAppCompactActivity<*>).onBackPressedDispatcher.onBackPressed()
                // Send broadcast
                requireContext().sendBroadcast(Intent(INTENT_PROFILE))
            }
        })

        mViewModel.onGetAccountDetail().observe(this, Observer {
            binding.etAccountNumber.setText("XXXXXXXX${it.accountNumber}")
            binding.etAccountHolderName.setText(it.accountHolderName)
            binding.etIfscCode.setText(it.routingNumber)

        })

    }

    private fun createBankAccountToken(bankAccountParams: BankAccountTokenParams) {
        val stripe = Stripe(
            requireContext(), BuildConfig.STRIPE_LIVE_KEY
        ) // Replace with your Stripe Publishable Key

        stripe.createBankAccountToken(bankAccountParams,
            callback = object :
                com.stripe.android.ApiResultCallback<com.stripe.android.model.Token> {
                override fun onSuccess(result: com.stripe.android.model.Token) {
                    Log.e("success", "onSuccess: " + result.id)
                    hideProgressLoader()

                    // Api call
                    mViewModel.addAccountDetail(result.id)
                }

                override fun onError(e: Exception) {
                    hideProgressLoader()
                    showMessage(
                        null, e.message, isShowSnackbarMessage = true, isError = true
                    )
                }
            })
    }

    private fun initSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.stripe_supported_countries,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnrCountry.adapter = adapter

        // Handle selection
        binding.spnrCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                // Get the selected item
                val selectedItem: String = parent?.getItemAtPosition(position).toString()

                // Split the string to extract the country code (e.g., "ZW" from "Zimbabwe (ZW)")
                country = selectedItem.substring(
                    selectedItem.lastIndexOf("(") + 1, selectedItem.lastIndexOf(")")
                )

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case when no selection is made
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvSkip -> {
                requireContext().startActivity(
                    Intent(requireContext(), HomeActivity::class.java)
                )
                (activity as BaseAppCompactActivity<*>).finish()
            }

            R.id.btnSubmit -> {
                // Check validations
                when {
                    binding.etAccountHolderName.text.toString().trim().isBlank() -> {
                        showMessage(
                            null,
                            getString(R.string.st_empty_account_holder_name),
                            isShowSnackbarMessage = true,
                            true
                        )
                    }

                    binding.etAccountNumber.text.toString().trim().isBlank() -> {
                        showMessage(null, getString(R.string.st_empty_account_number), true, true)
                    }

                    binding.etAccountNumber.text.toString().trim().contains("X") -> {
                        showMessage(null, getString(R.string.st_update_account_number), true, true)
                    }

                    binding.etIfscCode.text.toString().trim().isBlank() -> {
                        showMessage(
                            null,
                            getString(R.string.st_empty_IFSC),
                            isShowSnackbarMessage = true,
                            true
                        )
                    }

                    country.isBlank() -> {
                        showMessage(
                            null,
                            getString(R.string.st_empty_country),
                            isShowSnackbarMessage = true,
                            true
                        )
                    }

                    else -> {
                        showProgressLoader()
                        // Generate BankAccountParams
                        val bankAccountParams = BankAccountTokenParams(
                            country = country,
                            currency = currency,
                            accountHolderName = binding.etAccountHolderName.text.toString().trim(),
                            accountHolderType = BankAccountTokenParams.Type.Individual,
                            accountNumber = binding.etAccountNumber.text.toString().trim(),
                            routingNumber = binding.etIfscCode.text.toString().trim()
                        )

                        createBankAccountToken(
                            bankAccountParams
                        )

                    }

                }
            }

        }
    }

}
