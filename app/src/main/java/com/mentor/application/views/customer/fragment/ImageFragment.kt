package com.mentor.application.views.customer.fragment

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.mentor.application.databinding.FragmentImageBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.swingby.app.views.fragments.base.BaseFragment
import java.io.File


/**
 * Created by Mukesh on 03/06/2016.
 */
class ImageFragment : BaseFragment<FragmentImageBinding>(FragmentImageBinding::inflate) {

    companion object {
        const val BUNDLE_EXTRAS_IMAGE = "image"

        fun newInstance(image: String): ImageFragment {
            val imageFragment = ImageFragment()
            val bundle = Bundle()
            bundle.putString(BUNDLE_EXTRAS_IMAGE, image)
            imageFragment.arguments = bundle
            return imageFragment
        }
    }


    lateinit var player: SimpleExoPlayer

    private var previewMedia = ""

    override fun init(savedInstanceState: Bundle?) {
        // Get image from arguments
        previewMedia = arguments?.getString(BUNDLE_EXTRAS_IMAGE).toString()

        //Initialize player
        player = ExoPlayerFactory.newSimpleInstance(requireContext())

        if (GeneralFunctions.isVideo(previewMedia)) {
            binding.playerView.visibility = View.VISIBLE
            binding.zdvImage.visibility = View.GONE
            setVideoView()
        } else {
            binding.playerView.visibility = View.GONE
            binding.zdvImage.visibility = View.VISIBLE

            // check if image is a local file or url
            val mImageFile =
                GeneralFunctions.getLocalMediaFile(requireContext(), File(previewMedia).name)

            setImage(
                if (mImageFile.exists()) {
                    GeneralFunctions.getLocalImageFile(mImageFile)
                } else {
                    GeneralFunctions.getImage(previewMedia)
                }
            )
        }


    }

    private fun setVideoView() {
        val mediaDataSourceFactory = DefaultDataSourceFactory(
            requireContext(),
            Util.getUserAgent(requireContext(), "mediaPlayerSample")
        )

        // check if image is a local file or url

        val mImageFile =
            GeneralFunctions.getLocalMediaFile(requireContext(), File(previewMedia).name)

        val path = if (mImageFile.exists()) {
            GeneralFunctions.getLocalImageFile(mImageFile)
        } else {
            GeneralFunctions.getImage(previewMedia)
        }

        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory)
            .createMediaSource(Uri.parse(path))

        with(player) {
            prepare(mediaSource, false, false)
            playWhenReady = true
        }

        binding.playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        binding.playerView.player = player
        player.repeatMode = Player.REPEAT_MODE_ALL
        binding.playerView.requestFocus()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        player.stop()
    }

    override fun onPause() {
        super.onPause()
        player.stop()
    }


    private fun setImage(image: String) {
        binding.zdvImage.setAllowTouchInterceptionWhileZoomed(true)
        val controller = Fresco.newDraweeControllerBuilder()
            .setUri(Uri.parse(image))
            .build()
        binding.zdvImage.controller = controller
    }

    override val viewModel: BaseViewModel?
        get() = null

    override fun observeProperties() {
    }

    override val toolbar: ToolbarBinding?
        get() = null

}

