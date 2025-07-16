package com.mentor.application.views.comman.dialgofragments

import android.os.Bundle
import android.view.View
import com.mentor.application.R
import com.mentor.application.databinding.DialogFragmentImagePreviewBinding
import com.mentor.application.databinding.ToolbarDialogFragmentsBinding
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.views.comman.adapters.ImagePreviewAdatper

class ImagePreviewDialogFragment :
    BaseDialogFragment<DialogFragmentImagePreviewBinding>(DialogFragmentImagePreviewBinding::inflate),
    View.OnClickListener {

    companion object {
        const val BUNDLE_EXTRA_IMAGES_LIST = "imagesList"
        const val BUNDLE_EXTRA_IMAGE_POSITION = "imagePosition"

        fun newInstance(
            imagesList: ArrayList<String>,
            position: Int
        ): ImagePreviewDialogFragment {
            val imagePreviewDialogFragment = ImagePreviewDialogFragment()
            val bundle = Bundle()
            bundle.putStringArrayList(BUNDLE_EXTRA_IMAGES_LIST, imagesList)
            bundle.putInt(BUNDLE_EXTRA_IMAGE_POSITION, position)
            imagePreviewDialogFragment.arguments = bundle
            return imagePreviewDialogFragment
        }
    }

    override fun init() {
        // Get bundle data from arguments
        if (null != arguments) {
            val imagesList = requireArguments()
                .getStringArrayList(BUNDLE_EXTRA_IMAGES_LIST)

            if (null != imagesList) {
                binding.viewPager.adapter = ImagePreviewAdatper(
                    fragmentManager = childFragmentManager,
                    imagesList = imagesList
                )

                if (1 == imagesList.size) {
                    binding.circlePagerIndicator.visibility = View.GONE
                } else {
                    binding.circlePagerIndicator.visibility = View.VISIBLE
                    binding.circlePagerIndicator.setViewPager(binding.viewPager)
                    binding.viewPager.currentItem =
                        requireArguments().getInt(BUNDLE_EXTRA_IMAGE_POSITION, 0)
                }
            }
        }

        //Set click listener
        binding.ivBack.setOnClickListener(this)
    }

    override val isFullScreenDialog: Boolean
        get() = true

    override val toolbar: ToolbarDialogFragmentsBinding?
        get() = null

    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                dismiss()
            }
        }
    }
}