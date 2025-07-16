package com.mentor.application.views.customer.fragment

import android.os.Bundle
import android.view.MenuItem
import com.mentor.application.R
import com.mentor.application.databinding.FragmentDashboardBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.Tab
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.comman.adapters.TabsAdapter
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DashboardFragment :
    BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    companion object {
        const val BUNDLE_TAB_NUMBER = "tabNumber"
        const val BUNDLE_TAB_HOME = 0
        const val BUNDLE_TAB_BOOKINGS = 1

        fun newInstance(type: Int): DashboardFragment {
            val args = Bundle()
            val fragment = DashboardFragment()
            args.putInt(BUNDLE_TAB_NUMBER, type)
            fragment.arguments = args
            return fragment
        }
    }


    private lateinit var mCurrentSelectedMenuItem: MenuItem

    private var mTabNumber = BUNDLE_TAB_HOME

    override val toolbar: ToolbarBinding?
        get() = null

    override fun init(savedInstanceState: Bundle?) {

        // Get arguments
        arguments?.let {
            mTabNumber = it.getInt(BUNDLE_TAB_NUMBER, BUNDLE_TAB_HOME)
        }

        // Set viewPager
        val tabsList = mutableListOf<Tab>()
        tabsList.add(
            Tab(
                tabFragment = HomeFragment(),
                tabName = ""
            )
        )

        tabsList.add(
            Tab(
                tabFragment = MyBookingsFragment(),
                tabName = ""
            )
        )
        tabsList.add(
            Tab(
                tabFragment = ProfileFragment(),
                tabName = ""
            )
        )

        val tabsAdapter = TabsAdapter(context = this, tabsList = tabsList)
        binding.viewPager.adapter = tabsAdapter
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.isUserInputEnabled = false

        // Set bottomNavigationView
        binding.bottomNavigationView.itemIconTintList = null
        mCurrentSelectedMenuItem = binding.bottomNavigationView.menu.getItem(0)
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.actionHome -> {
                    binding.viewPager.setCurrentItem(0, false)
                    binding.bottomNavigationView.menu.getItem(0)
                        .setIcon(R.drawable.ic_home_active_tab)
                    binding.bottomNavigationView.menu.getItem(1)
                        .setIcon(R.drawable.ic_calender_inactive_tab)
                    binding.bottomNavigationView.menu.getItem(2)
                        .setIcon(R.drawable.ic_profile_inactive_tab)


                }

                R.id.actionMyBooking -> {
                    binding.viewPager.setCurrentItem(1, false)
                    binding.bottomNavigationView.menu.getItem(0)
                        .setIcon(R.drawable.ic_home_inactive)
                    binding.bottomNavigationView.menu.getItem(1)
                        .setIcon(R.drawable.ic_booking_active_tab)
                    binding.bottomNavigationView.menu.getItem(2)
                        .setIcon(R.drawable.ic_profile_inactive_tab)


                }

                R.id.actionProfile -> {
                    binding.viewPager.setCurrentItem(2, false)
                    binding.bottomNavigationView.menu.getItem(0)
                        .setIcon(R.drawable.ic_home_inactive)
                    binding.bottomNavigationView.menu.getItem(1)
                        .setIcon(R.drawable.ic_calender_inactive_tab)
                    binding.bottomNavigationView.menu.getItem(2)
                        .setIcon(R.drawable.ic_profile_active_tab)


                }
            }
            true
        }

        if (mTabNumber == BUNDLE_TAB_BOOKINGS) {
            binding.viewPager.setCurrentItem(1, false)
            binding.bottomNavigationView.setSelectedItemId(R.id.actionMyBooking);
        }

    }

    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {

    }

}
