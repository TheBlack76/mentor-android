package com.mentor.application.views.customer.fragment

import android.os.Bundle
import com.mentor.application.R
import com.mentor.application.databinding.FragmentChildLaunchBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.swingby.app.views.fragments.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChildLaunchFragment :
    BaseFragment<FragmentChildLaunchBinding>(FragmentChildLaunchBinding::inflate) {

    companion object {
        internal const val BUNDLE_POSITION = "position"

        fun newInstance(position: Int): ChildLaunchFragment {
            val imageFragment = ChildLaunchFragment()
            val bundle = Bundle()
            bundle.putInt(BUNDLE_POSITION, position)
            imageFragment.arguments = bundle
            return imageFragment
        }
    }

    override val toolbar: ToolbarBinding?
        get() = null

    override fun init(savedInstanceState: Bundle?) {
        when (arguments?.getInt(BUNDLE_POSITION)) {
            0 -> {
                binding.sdvImageView.setImageResource(R.drawable.ic_launch_img_1)
            }
            1 -> {
                binding.sdvImageView.setImageResource(R.drawable.ic_launch_img_2)
            }
            2 -> {
                binding.sdvImageView.setImageResource(R.drawable.ic_launch_img_3)
            }
        }
    }

    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {

    }

}
