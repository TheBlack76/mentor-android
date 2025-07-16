package com.mentor.application.views.comman.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mentor.application.R
import com.mentor.application.databinding.FragmentMessageBinding
import com.mentor.application.databinding.ToolbarBinding
import com.mentor.application.repository.models.Message
import com.mentor.application.utils.AmazonS3
import com.mentor.application.utils.AmazonS3.Companion.S3_CHAT_PHOTOS
import com.mentor.application.utils.AmazonS3.Companion.SERVER_CHAT_PHOTOS
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.BaseViewModel
import com.mentor.application.viewmodels.comman.MessageViewModel
import com.mentor.application.views.comman.adapters.MessagesListAdapter
import com.mentor.application.views.comman.dialgofragments.ImagePreviewDialogFragment
import com.mentor.application.views.customer.interfaces.MessagesInterface
import com.swingby.app.views.fragments.base.BasePictureOptionsFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class MessageFragment :
    BasePictureOptionsFragment<FragmentMessageBinding>(FragmentMessageBinding::inflate),
    OnClickListener,
    MessagesInterface {

    companion object {
        const val BUNDLE_BOOKING_ID = "bookingId"
        const val BUNDLE_NAME = "name"

        fun newInstance(bookingId: String, name: String): MessageFragment {
            val args = Bundle()
            val fragment = MessageFragment()
            args.putString(BUNDLE_BOOKING_ID, bookingId)
            args.putString(BUNDLE_NAME, name)
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var mMessagesListAdapter: MessagesListAdapter

    private val mMessageViewModel: MessageViewModel by viewModels()

    private var mSkip = 0

    private var mBookingId = ""
    private var mName = ""

    override val toolbar: ToolbarBinding?
        get() = binding.appBarLayout

    override fun setData(savedInstanceState: Bundle?) {
        // Get arguments
        arguments?.let {
            mBookingId = it.getString(BUNDLE_BOOKING_ID) ?: ""
            mName = it.getString(BUNDLE_NAME) ?: ""
        }

        // Set toolbar
        binding.appBarLayout.tvToolbarTitle.text = mName
        val params =
            binding.appBarLayout.tvToolbarTitle.layoutParams as androidx.appcompat.widget.Toolbar.LayoutParams
        params.gravity = Gravity.START  // Set the gravity to START or any desired value
        binding.appBarLayout.tvToolbarTitle.layoutParams = params

        // Set adapter
        binding.recyclerView.adapter = mMessagesListAdapter

        // Set click listener
        binding.ivGallery.setOnClickListener(this)
        binding.ivSend.setOnClickListener(this)
        binding.swipeRefreshLayout.isEnabled = false

        mMessageViewModel.saveChatInformation(mBookingId)

        // Api call
        mMessageViewModel.getChat(mBookingId, mSkip)

    }

    override fun onGettingImageFile(file: File) {
        mMessageViewModel.sendImage(
            SERVER_CHAT_PHOTOS + file.name
        )

        // Upload image to S3
        if (file.absolutePath.let { GeneralFunctions.isRemoteImage(it) }) {
            AmazonS3(requireContext()).uploadFileToS3(
                file,
                S3_CHAT_PHOTOS

            ) { isSuccess ->
                if (isSuccess) {
                    // Upload media to server
                    mMessageViewModel.sendImageToServer(SERVER_CHAT_PHOTOS + file.name)
                    mMessagesListAdapter.imageUploaded(SERVER_CHAT_PHOTOS + file.name)
                    // File uploaded successfully
                } else {
                    showMessage(
                        null,
                        getString(R.string.st_media_not_uploaded_please_try_again)
                    )
                }
            }
        }
    }


    override val viewModel: BaseViewModel?
        get() = mMessageViewModel

    override fun observeProperties() {

        mMessageViewModel.onGetMessageRealTime().observe(this, Observer {
            // Update data
            mMessagesListAdapter.upDateData(message = it)
            binding.recyclerView.scrollToPosition(mMessagesListAdapter.getChatListCount())
            binding.tvNoData.visibility = View.GONE
        })

        mMessageViewModel.onGetMessage().observe(this, Observer {
            // Update data
            mMessagesListAdapter.upDateData(messagesList = it as List<Message>, mPage = mSkip)
            binding.recyclerView.scrollToPosition(it.size)
        })

        mMessageViewModel.isShowSwipeRefreshLayout().observe(this, Observer {
            binding.swipeRefreshLayout.isRefreshing = it
        })

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.ivSend -> {
                if (binding.etMessage.text.toString().trim().isBlank()) {
                    showMessage(null, getString(R.string.st_empty_message), true)
                } else {
                    //Call api
                    mMessageViewModel.sendMessage(
                        binding.etMessage.text.toString().trim()

                    )
                    binding.etMessage.setText("")
                }
            }

            R.id.ivGallery -> {
                showPictureOptionsBottomSheet(GeneralFunctions.getOutputDirectory(requireContext()).absolutePath)

            }
        }
    }

    override fun onLoadMore() {
        mSkip += 1
        mMessageViewModel.getChat(mBookingId, mSkip)
    }

    override fun onImageClick(message: Message) {
        val mImageList = ArrayList<String>()
        mImageList.add(message.message)
        ImagePreviewDialogFragment.newInstance(mImageList, 0).show(childFragmentManager, "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMessageViewModel.eraseChatInformation()

    }

}
