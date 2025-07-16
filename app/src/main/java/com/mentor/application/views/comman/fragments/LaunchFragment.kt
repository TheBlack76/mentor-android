package com.mentor.application.views.comman.fragments

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.AnimationUtils
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.mentor.application.R
import com.mentor.application.databinding.FragmentLaunchBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.comman.activities.BaseAppCompactActivity
import com.mentor.application.views.comman.activities.doFragmentTransaction
import com.mentor.application.views.comman.adapters.LaunchTabsAdapter
import com.mentor.application.views.comman.utils.DepthPageTransformer
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LaunchFragment : BaseFragment<FragmentLaunchBinding>(FragmentLaunchBinding::inflate),
    OnClickListener {

    private var currentPageIndex = 0

    override val toolbar: ToolbarBinding?
        get() = null

    override fun init(savedInstanceState: Bundle?) {
        //Set adapter
        binding.viewPager.adapter = LaunchTabsAdapter(
            fragmentManager = requireActivity()
        )

        binding.viewPager.setPageTransformer(DepthPageTransformer())

        binding.indicatorView.attachTo(binding.viewPager)
        binding.viewPager.offscreenPageLimit = 1

        //ViewPager Listener
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
                    0 -> {
                        binding.tvHeading.text = getString(R.string.st_info_header_1)
                        binding.tvTagline.text = getString(R.string.st_info_tagline_1)

                    }

                    1 -> {
                        binding.tvHeading.text = getString(R.string.st_info_header_2)
                        binding.tvTagline.text = getString(R.string.st_info_tagline_2)

                    }

                    2 -> {
                        binding.tvHeading.text = getString(R.string.st_info_header_3)
                        binding.tvTagline.text = getString(R.string.st_info_tagline_3)
                    }

                }

                val animObj = if (position > currentPageIndex) {
                    AnimationUtils.loadAnimation(requireContext(), R.anim.anim_smooth_right_to_left)

                } else {
                    AnimationUtils.loadAnimation(requireContext(), R.anim.anim_smooth_left_to_right)

                }

                animObj.duration = 500
                binding.tvHeading.startAnimation(animObj)
                binding.tvTagline.startAnimation(animObj)
                currentPageIndex = position


            }
        })

        //Set click listener
        binding.btnGetStarted.setOnClickListener(this)

    }


    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {

    }

    override fun onResume() {
        super.onResume()
        (activityContext as BaseAppCompactActivity<*>).changeStatusBarColor(R.color.colorWhite)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnGetStarted -> {
                if (currentPageIndex == 0) {
                    (activity as BaseAppCompactActivity<*>).doFragmentTransaction(
                        fragment = SelectUserTypeFragment(),
                        containerViewId = R.id.flFragContainerMain,
                        enterAnimation = R.animator.slide_right_in_fade_in,
                        exitAnimation = R.animator.scale_fade_out,
                        popExitAnimation = R.animator.slide_right_out_fade_out
                    )
                } else {
                    binding.viewPager.setCurrentItem(currentPageIndex + 1, true)
                }

            }
        }
    }

}
