package com.mentor.application.views.comman.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mentor.application.repository.models.Tab


class Tabs2Adapter(context: FragmentActivity, private val tabsList: List<Tab>) :
    FragmentStateAdapter(context) {

    override fun getItemCount(): Int =  tabsList.size

    override fun createFragment(position: Int): Fragment = tabsList[position].tabFragment!!

}
