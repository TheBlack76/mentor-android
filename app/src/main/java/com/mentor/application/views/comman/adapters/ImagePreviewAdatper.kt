package com.mentor.application.views.comman.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.mentor.application.views.customer.fragment.ImageFragment

class ImagePreviewAdatper(
    fragmentManager: FragmentManager, behaviour: Int = 0,
    private val imagesList: ArrayList<String>
) :
    FragmentStatePagerAdapter(fragmentManager, behaviour) {

    override fun getItem(position: Int): Fragment =
        ImageFragment.newInstance(imagesList[position])

    override fun getCount(): Int = imagesList.size
}