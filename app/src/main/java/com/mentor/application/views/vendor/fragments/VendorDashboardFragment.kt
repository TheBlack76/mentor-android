package com.mentor.application.views.vendor.fragments

import android.os.Bundle
import android.view.MenuItem
import com.mentor.application.R
import com.mentor.application.databinding.FragmentVendorDashboardBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.Tab
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.comman.adapters.TabsAdapter
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VendorDashboardFragment :
    BaseFragment<FragmentVendorDashboardBinding>(FragmentVendorDashboardBinding::inflate) {

    private lateinit var mCurrentSelectedMenuItem: MenuItem

    override val toolbar: ToolbarBinding?
        get() = null

    override fun init(savedInstanceState: Bundle?) {

        // Set viewPager
        val tabsList = mutableListOf<Tab>()
        tabsList.add(
            Tab(
                tabFragment = VendorHomeFragment(),
                tabName = ""
            )
        )

        tabsList.add(
            Tab(
                tabFragment = VendorProfileFragment(),
                tabName = ""
            )
        )
        tabsList.add(
            Tab(
                tabFragment = SettingsFragment(),
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
                        .setIcon(R.drawable.ic_profile_inactive_tab)

                    binding.bottomNavigationView.menu.getItem(2)
                        .setIcon(R.drawable.ic_setting_inactive)


                }

                R.id.actionProfile -> {
                    binding.viewPager.setCurrentItem(1, false)
                    binding.bottomNavigationView.menu.getItem(0)
                        .setIcon(R.drawable.ic_home_inactive)

                    binding.bottomNavigationView.menu.getItem(1)
                        .setIcon(R.drawable.ic_profile_active_tab)

                    binding.bottomNavigationView.menu.getItem(2)
                        .setIcon(R.drawable.ic_setting_inactive)

                }

                R.id.actionSetting -> {
                    binding.viewPager.setCurrentItem(2, false)
                    binding.bottomNavigationView.menu.getItem(0)
                        .setIcon(R.drawable.ic_home_inactive)

                    binding.bottomNavigationView.menu.getItem(1)
                        .setIcon(R.drawable.ic_profile_inactive_tab)

                    binding.bottomNavigationView.menu.getItem(2)
                        .setIcon(R.drawable.ic_setting_active)


                }

            }
            true
        }

    }

    fun initSetting(){
        binding.bottomNavigationView.setSelectedItemId(R.id.actionSetting)
        binding.viewPager.setCurrentItem(2,true)
    }

    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {

    }

}
