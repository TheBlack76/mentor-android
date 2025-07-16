package com.mentor.application.views.comman.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mentor.application.views.customer.fragment.ChildLaunchFragment

class LaunchTabsAdapter(fragmentManager: FragmentActivity) :
    FragmentStateAdapter(fragmentManager) {

    override fun getItemCount(): Int = 1

    override fun createFragment(position: Int): Fragment = ChildLaunchFragment.newInstance(position)


}