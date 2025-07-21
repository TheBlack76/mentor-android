package com.mentor.application.views.vendor.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.mentor.application.R
import com.mentor.application.databinding.FragmentVendorHomeBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.Tab
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.OnBoardingViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.comman.adapters.Tabs2Adapter
import com.mentor.application.views.customer.fragment.EditProfileFragment
import com.mentor.application.views.customer.fragment.HomeFragment.Companion.INTENT_HOME
import com.mentor.application.views.customer.fragment.HomeFragment.Companion.INTENT_NOTIFICATION
import com.mentor.application.views.customer.fragment.NotificationFragment
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.NEW_BOOKINGS
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.PAST_BOOKINGS
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.UPCOMING_BOOKINGS
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class VendorHomeFragment :
    BaseFragment<FragmentVendorHomeBinding>(FragmentVendorHomeBinding::inflate), OnClickListener {


    private val mOnBoardingViewModel: OnBoardingViewModel by viewModels()


    override val toolbar: ToolbarBinding?
        get() = null

    override fun init(savedInstanceState: Bundle?) {

        // Initialize receiver
        requireContext().registerReceiver(
            mGetUpdateDataBroadcastReceiver, IntentFilter(INTENT_HOME), Context.RECEIVER_EXPORTED
        )

        requireContext().registerReceiver(
            mGetNotificationCount, IntentFilter(INTENT_NOTIFICATION), Context.RECEIVER_EXPORTED
        )


        // Set click listener
        binding.btnNew.setOnClickListener(this)
        binding.btnUpcoming.setOnClickListener(this)
        binding.btnPast.setOnClickListener(this)
        binding.ivNotification.setOnClickListener(this)
        binding.accountDetail.tvProfileInfo.setOnClickListener(this)
        binding.accountDetail.tvSubcategory.setOnClickListener(this)
        binding.accountDetail.tvRates.setOnClickListener(this)
        binding.accountDetail.tvAddAvailability.setOnClickListener(this)
        binding.accountDetail.tvAddBankInfo.setOnClickListener(this)

        // Set user detail
        setUserData()

        // Set viewPager
        val tabsList = mutableListOf<Tab>()
        tabsList.add(
            Tab(
                tabFragment = NewRequestFragment.newInstance(NEW_BOOKINGS), tabName = ""
            )
        )

        tabsList.add(
            Tab(
                tabFragment = NewRequestFragment.newInstance(UPCOMING_BOOKINGS), tabName = ""
            )
        )
        tabsList.add(
            Tab(
                tabFragment = NewRequestFragment.newInstance(PAST_BOOKINGS), tabName = ""
            )
        )


        val tabsAdapter = Tabs2Adapter(context = requireActivity(), tabsList = tabsList)
        binding.viewPager.adapter = tabsAdapter

        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> initNew()
                    1 -> initUpcoming()
                    2 -> initPast()

                }
            }

        })

        mOnBoardingViewModel.getMessageCount()
    }

    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {
        mOnBoardingViewModel.onGetNotificationCount().observe(this, Observer {
            if (it > 0) {
                binding.tvNotificationCount.visibility = View.VISIBLE
            } else {
                binding.tvNotificationCount.visibility = View.GONE
            }
        })

    }

    fun setUserData() {
        // If the file exist with same name in local show from local else from server
        val mImageFile = GeneralFunctions.getLocalMediaFile(
            requireContext(), File(mUserPrefsManager.loginUser?.image ?: "").name
        )

        val path = if (mImageFile.exists()) {
            GeneralFunctions.getLocalImageFile(mImageFile)
        } else {
            GeneralFunctions.getUserImage(mUserPrefsManager.loginUser?.image ?: "")
        }
        mUserPrefsManager.loginUser?.let {
            binding.sdvUserImage.setImageURI(path)
            binding.tvUserName.text = it.fullName
        }

      // Check user fill there require info to get the booking requests
        if (mUserPrefsManager.loginUser?.isRegister == false || mUserPrefsManager.loginUser?.isAvailability == false || mUserPrefsManager.loginUser?.isBankAccount == false) {
            binding.accountDetail.clGuestView.visibility = View.VISIBLE
        } else {
            binding.accountDetail.clGuestView.visibility = View.GONE
        }

        if (mUserPrefsManager.loginUser?.fullName!!.isBlank() || mUserPrefsManager.loginUser?.mobileNumber!!.isBlank()) {
            isDataFilled(binding.accountDetail.tvProfileInfo, false)
        } else {
            isDataFilled(binding.accountDetail.tvProfileInfo, true)
        }

        isDataFilled(
            binding.accountDetail.tvSubcategory, mUserPrefsManager.loginUser?.isRegister == true
        )
        isDataFilled(binding.accountDetail.tvRates, mUserPrefsManager.loginUser?.isRegister == true)
        isDataFilled(
            binding.accountDetail.tvAddAvailability,
            mUserPrefsManager.loginUser?.isAvailability == true
        )
        isDataFilled(
            binding.accountDetail.tvAddBankInfo, mUserPrefsManager.loginUser?.isBankAccount == true
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnNew -> {
                initNew()
                binding.viewPager.setCurrentItem(0, true)
            }

            R.id.btnUpcoming -> {
                initUpcoming()
                binding.viewPager.setCurrentItem(1, true)

            }

            R.id.btnPast -> {
                initPast()
                binding.viewPager.setCurrentItem(2, true)

            }

            R.id.tvProfileInfo -> {
                doTransaction(EditProfileFragment())
            }

            R.id.tvSubcategory -> {
                doTransaction(EnterDetailFragment.newInstance(EnterDetailFragment.BUNDLE_VIEW_TYPE_EDIT))

            }

            R.id.tvRates -> {
                doTransaction(EnterDetailFragment.newInstance(EnterDetailFragment.BUNDLE_VIEW_TYPE_EDIT))

            }

            R.id.tvAddAvailability -> {
                doTransaction(MyAvailabilityFragment())

            }

            R.id.tvAddBankInfo -> {
                doTransaction(AccountDetailSetupFragment.newInstance(AccountDetailSetupFragment.BUNDLE_VIEW_TYPE_EDIT))

            }

            R.id.ivNotification -> {
                (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                    fragment = NotificationFragment(),
                    containerViewId = R.id.flFragContainerMain,
                    enterAnimation = R.animator.slide_right_in_fade_in,
                    exitAnimation = R.animator.scale_fade_out,
                    popExitAnimation = R.animator.slide_right_out_fade_out
                )
            }
        }
    }

    private fun initNew() {
        binding.btnNew.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), R.color.colorPrimaryPeach
        )
        binding.btnNew.strokeColor =
            ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
        binding.btnNew.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.font_roboto_medium)

        binding.btnUpcoming.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), R.color.colorWhite
        )
        binding.btnUpcoming.strokeColor =
            ContextCompat.getColorStateList(requireContext(), R.color.colorPrimaryText)
        binding.btnUpcoming.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.font_roboto_regular)

        binding.btnPast.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), R.color.colorWhite
        )
        binding.btnPast.strokeColor =
            ContextCompat.getColorStateList(requireContext(), R.color.colorPrimaryText)
        binding.btnPast.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.font_roboto_regular)

    }


    private fun initUpcoming() {
        binding.btnUpcoming.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), R.color.colorPrimaryPeach
        )
        binding.btnUpcoming.strokeColor =
            ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
        binding.btnUpcoming.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.font_roboto_medium)

        binding.btnNew.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), R.color.colorWhite
        )
        binding.btnNew.strokeColor =
            ContextCompat.getColorStateList(requireContext(), R.color.colorPrimaryText)
        binding.btnNew.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.font_roboto_regular)

        binding.btnPast.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), R.color.colorWhite
        )
        binding.btnPast.strokeColor =
            ContextCompat.getColorStateList(requireContext(), R.color.colorPrimaryText)
        binding.btnPast.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.font_roboto_regular)

    }

    private fun initPast() {
        binding.btnUpcoming.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), R.color.colorWhite
        )
        binding.btnUpcoming.strokeColor =
            ContextCompat.getColorStateList(requireContext(), R.color.colorPrimaryText)
        binding.btnUpcoming.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.font_roboto_regular)

        binding.btnNew.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), R.color.colorWhite
        )
        binding.btnNew.strokeColor =
            ContextCompat.getColorStateList(requireContext(), R.color.colorPrimaryText)
        binding.btnNew.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.font_roboto_regular)

        binding.btnPast.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(), R.color.colorPrimaryPeach
        )
        binding.btnPast.strokeColor =
            ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
        binding.btnPast.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.font_roboto_medium)

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

    private fun isDataFilled(item: TextView, isFilled: Boolean) {
        if (isFilled) {
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic__checkbox)
            drawable?.setBounds(
                0, 0, GeneralFunctions.dpToPx(22), GeneralFunctions.dpToPx(22)
            ) // Custom size: 24dp × 24dp

            item.setCompoundDrawables(null, null, drawable, null)
            item.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryText))
            item.isEnabled = false
        } else {
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_right_arrow)
            drawable?.setBounds(
                0, 0, GeneralFunctions.dpToPx(24), GeneralFunctions.dpToPx(24)
            ) // Custom size: 24dp × 24dp

            item.setCompoundDrawables(null, null, drawable, null)
            item.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDisabledText))
            item.isEnabled = true


        }


    }


    private val mGetUpdateDataBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, p1: Intent?) {
            context?.let {
                try {
                    setUserData()
                } catch (e: Exception) {
                    println(e)

                }
            }
        }
    }

    private val mGetNotificationCount = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, p1: Intent?) {
            context?.let {
                try {
                    mOnBoardingViewModel.getMessageCount()
                } catch (e: Exception) {
                    println(e)

                }
            }
        }
    }

}
