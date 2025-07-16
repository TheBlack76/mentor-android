package com.mentor.application.views.customer.fragment

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.mentor.application.R
import com.mentor.application.databinding.FragmentMyBookingsBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.Tab
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.comman.adapters.Tabs2Adapter
import com.mentor.application.views.customer.fragment.UpComingBookingsFragment.Companion.PAST_BOOKING
import com.mentor.application.views.customer.fragment.UpComingBookingsFragment.Companion.UPCOMING_BOOKINGS
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyBookingsFragment :
    BaseFragment<FragmentMyBookingsBinding>(FragmentMyBookingsBinding::inflate),
    OnClickListener {

        override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun init(savedInstanceState: Bundle?) {
        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = getString(R.string.st_my_bookings)
        binding.appBarLayout.toolbar.navigationIcon = null

        // Set click listener
        binding.btnUpcoming.setOnClickListener(this)
        binding.btnPast.setOnClickListener(this)

        // Set viewPager
        val tabsList = mutableListOf<Tab>()
        tabsList.add(
            Tab(
                tabFragment = UpComingBookingsFragment.newInstance(UPCOMING_BOOKINGS),
                tabName = ""
            )
        )

        tabsList.add(
            Tab(
                tabFragment = UpComingBookingsFragment.newInstance(PAST_BOOKING),
                tabName = ""
            )
        )


        val tabsAdapter = Tabs2Adapter(context = requireActivity(), tabsList = tabsList)
        binding.viewPager.adapter = tabsAdapter
//        binding.viewPager.offscreenPageLimit = 2

        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> initUpcoming()
                    1 -> initPast()

                }
            }

        })
    }

    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnUpcoming -> {
                initUpcoming()
                binding.viewPager.setCurrentItem(0, true)

            }

            R.id.btnPast -> {
                initPast()
                binding.viewPager.setCurrentItem(1, true)


            }

        }
    }

    private fun initUpcoming() {
        binding.btnUpcoming.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            R.color.colorPrimaryPeach
        )
        binding.btnUpcoming.strokeColor =
            ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
        binding.btnUpcoming.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.font_roboto_medium)

        binding.btnPast.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            R.color.colorWhite
        )
        binding.btnPast.strokeColor =
            ContextCompat.getColorStateList(requireContext(), R.color.colorPrimaryText)
        binding.btnPast.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.font_roboto_regular)

    }

    private fun initPast() {
        binding.btnUpcoming.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            R.color.colorWhite
        )
        binding.btnUpcoming.strokeColor =
            ContextCompat.getColorStateList(requireContext(), R.color.colorPrimaryText)
        binding.btnUpcoming.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.font_roboto_regular)

        binding.btnPast.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            R.color.colorPrimaryPeach
        )
        binding.btnPast.strokeColor =
            ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
        binding.btnPast.typeface =
            ResourcesCompat.getFont(requireContext(), R.font.font_roboto_medium)

    }

}
