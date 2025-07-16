package com.mentor.application.views.comman.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mentor.application.R
import com.mentor.application.databinding.LayoutReceiverImageBinding
import com.mentor.application.databinding.LayoutReceiverTextMessageBinding
import com.mentor.application.databinding.LayoutSenderImageBinding
import com.mentor.application.databinding.LayoutSenderTextMessageBinding
import com.mentor.application.databinding.RowLoadMoreBinding
import com.mentor.application.repository.models.Message
import com.mentor.application.repository.preferences.UserPrefsManager
import com.mentor.application.utils.Constants
import com.mentor.application.utils.Constants.DATE_DISPLAY_TIME
import com.mentor.application.utils.Constants.DATE_FORMAT_DISPLAY
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.viewmodels.comman.MessageViewModel.Companion.EVENT_PARAM_MESSAGE_TYPE_TEXT
import com.mentor.application.views.customer.interfaces.MessagesInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@FragmentScoped
class MessagesListAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    var mUserPrefsManager: UserPrefsManager,
    var mListener: MessagesInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ROW_TYPE_SENDER_TEXT_MESSAGE = 1
        private const val ROW_TYPE_RECEIVER_TEXT_MESSAGE = 2
        private const val ROW_TYPE_SENDER_IMAGE_MESSAGE = 3
        private const val ROW_TYPE_RECEIVER_IMAGE_MESSAGE = 4
        private const val ROW_TYPE_LOAD_EARLIER_MESSAGE = 5
        const val LIMIT = 30

    }

    private var mList = mutableListOf<Message>()
    private var isLoadMore = false

    private val sdfTime: SimpleDateFormat by lazy {
        SimpleDateFormat(
            DATE_FORMAT_DISPLAY,
            Locale.US
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ROW_TYPE_SENDER_TEXT_MESSAGE -> {
                SenderTextMessageViewHolder(
                    LayoutSenderTextMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            ROW_TYPE_RECEIVER_TEXT_MESSAGE -> {
                ReceiverTextMessageViewHolder(
                    LayoutReceiverTextMessageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            ROW_TYPE_SENDER_IMAGE_MESSAGE -> {
                SenderImageMessageViewHolder(
                    LayoutSenderImageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            ROW_TYPE_RECEIVER_IMAGE_MESSAGE -> {
                ReceiverImageMessageViewHolder(
                    LayoutReceiverImageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> {
                LoadMoreViewHolder(
                    RowLoadMoreBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size + 1
    }

    fun getChatListCount(): Int {
        return mList.size
    }


    fun upDateData(message: Message? = null, messagesList: List<Message>? = null, mPage: Int = 0) {
        when {
            null == message && null == messagesList -> return
            null != message -> {
                if (message.message.isNotBlank()
                ) {
                    if (message._id.isBlank()) {
                        this.mList.add(message)
                        notifyItemInserted(this.mList.size)

                    } else {
                        if (mList.any { it.message == message.message }) {
                            this.mList.replaceAll {
                                if (it._id.isBlank() == true && it.message == message.message) message else it
                            }

                            notifyDataSetChanged()

                        } else {
                            this.mList.add(message)
                            notifyItemInserted(this.mList.size)
                        }

                    }
                } else {
                    if (message._id.isBlank()) {
                        this.mList.add(message)
                        notifyItemInserted(this.mList.size)
                    } else {
                        if (mList.any { it.message == message.message }) {
                            this.mList.replaceAll {
                                if (it._id?.isEmpty() == true && it.message == message.message) message else it
                            }
                            notifyDataSetChanged()

                        } else {
                            this.mList.add(message)
                            notifyItemInserted(this.mList.size)
                        }
                    }

                }
            }

            else -> {
                if (mPage == 0) {
                    mList.clear()
                }
                this.mList.addAll(0, messagesList!!)
                isLoadMore = messagesList.size >= LIMIT
                if (mPage == 0 && messagesList.isEmpty()) {
                    notifyDataSetChanged()
                } else {
                    notifyItemRangeInserted(0, messagesList.size)
                }
            }
        }
    }

    fun imageUploaded(name: String) {
        val index = mList.indexOfFirst { it.message == name }

        if (index != -1) {
            // Update the item at the found index
            mList[index].isSending = false


            notifyItemChanged(index + 1)
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            ROW_TYPE_LOAD_EARLIER_MESSAGE
        } else {
            when (mList[position - 1].sender) {
                mUserPrefsManager.loginUser?._id -> {
                    if (mList[position - 1].type == EVENT_PARAM_MESSAGE_TYPE_TEXT) {
                        ROW_TYPE_SENDER_TEXT_MESSAGE
                    } else {
                        ROW_TYPE_SENDER_IMAGE_MESSAGE
                    }

                }

                else -> {
                    if (mList[position - 1].type == EVENT_PARAM_MESSAGE_TYPE_TEXT) {
                        ROW_TYPE_RECEIVER_TEXT_MESSAGE
                    } else {
                        ROW_TYPE_RECEIVER_IMAGE_MESSAGE
                    }
                }
            }

        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ROW_TYPE_SENDER_TEXT_MESSAGE -> {
                (holder as SenderTextMessageViewHolder)
                    .bindData(position - 1)
            }

            ROW_TYPE_SENDER_IMAGE_MESSAGE -> {
                (holder as SenderImageMessageViewHolder)
                    .bindData(position - 1)
            }

            ROW_TYPE_RECEIVER_TEXT_MESSAGE -> {
                (holder as ReceiverTextMessageViewHolder).bindData(position - 1)
            }

            ROW_TYPE_RECEIVER_IMAGE_MESSAGE -> {
                (holder as ReceiverImageMessageViewHolder)
                    .bindData(position - 1)
            }

            else -> {
                (holder as LoadMoreViewHolder)
                    .bindData(position - 1)
            }
        }
    }


    inner class SenderTextMessageViewHolder(val binding: LayoutSenderTextMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(position: Int) {

            //Set date header
            setDate(binding.timeHeader.tvTimeHeader, binding.timeHeader.clTimeHeader, position)

            binding.tvSenderMsg.text = mList[position].message

            if (mList[position].isSending){
                binding.tvTime.text = GeneralFunctions.changeDateFormat(
                    mList[position].createdAt,
                    Constants.DATE_FORMAT_SERVER,
                    DATE_DISPLAY_TIME
                )

            }else{
                binding.tvTime.text = GeneralFunctions.changeUtcToLocal(
                    mList[position].createdAt,
                    Constants.DATE_FORMAT_SERVER,
                    DATE_DISPLAY_TIME
                )

            }


        }
    }

    inner class SenderImageMessageViewHolder(val binding: LayoutSenderImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(position: Int) {

            //Set date header
            setDate(binding.timeHeader.tvTimeHeader, binding.timeHeader.clTimeHeader, position)

            // If the file exist with same name in local show from local else from server
            val mImageFile =
                GeneralFunctions.getLocalMediaFile(mContext!!, File(mList[position].message).name)

            val path = if (mImageFile.exists()) {
                GeneralFunctions.getLocalImageFile(mImageFile)
            } else {
                GeneralFunctions.getImage(mList[position].message)
            }

            // Use Glide to load the image into the ImageView
            Glide.with(mContext)
                .load(path) // Specify the image URL
                .placeholder(R.color.colorPlaceHolder) // Optional: Placeholder image
                .into(binding.sdvSenderImg)

            itemView.setOnClickListener {
                mListener.onImageClick(mList[position])
            }

            if (mList[position].isSending) {
                binding.pbImage.visibility = View.VISIBLE
            } else {
                binding.pbImage.visibility = View.GONE
            }


            if (mList[position].isSending){
                binding.tvTime.text = GeneralFunctions.changeDateFormat(
                    mList[position].createdAt,
                    Constants.DATE_FORMAT_SERVER,
                    DATE_DISPLAY_TIME
                )

            }else{
                binding.tvTime.text = GeneralFunctions.changeUtcToLocal(
                    mList[position].createdAt,
                    Constants.DATE_FORMAT_SERVER,
                    DATE_DISPLAY_TIME
                )

            }
        }
    }


    inner class ReceiverTextMessageViewHolder(val binding: LayoutReceiverTextMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(position: Int) {

            // Set date header
            setDate(binding.timeHeader.tvTimeHeader, binding.timeHeader.clTimeHeader, position)

            binding.tvSenderMsg.text = mList[position].message

            binding.tvTime.text = GeneralFunctions.changeUtcToLocal(
                mList[position].createdAt,
                Constants.DATE_FORMAT_SERVER,
                DATE_DISPLAY_TIME
            )

        }
    }

    inner class ReceiverImageMessageViewHolder(val binding: LayoutReceiverImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(position: Int) {

            // Set date header
            setDate(binding.timeHeader.tvTimeHeader, binding.timeHeader.clTimeHeader, position)

            // If the file exist with same name in local show from local else from server
            val mImageFile =
                GeneralFunctions.getLocalMediaFile(mContext!!, File(mList[position].message).name)


            val path = if (mImageFile.exists()) {
                GeneralFunctions.getLocalImageFile(mImageFile)
            } else {
                GeneralFunctions.getImage(mList[position].message)
            }

            // Use Glide to load the image into the ImageView
            Glide.with(mContext)
                .load(path) // Specify the image URL
                .placeholder(R.color.colorPlaceHolder) // Optional: Placeholder image
                .into(binding.sdvSenderImg)

            binding.tvTime.text = GeneralFunctions.changeUtcToLocal(
                mList[position].createdAt,
                Constants.DATE_FORMAT_SERVER,
                DATE_DISPLAY_TIME
            )

            itemView.setOnClickListener {
                mListener.onImageClick(mList[position])
            }

        }
    }

    inner class LoadMoreViewHolder(val binding: RowLoadMoreBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(position: Int) {
            if (isLoadMore) {
                binding.progressBar.visibility = View.VISIBLE
                mListener.onLoadMore()
            } else {

                binding.progressBar.visibility = View.GONE

            }
        }
    }

    private fun setDate(tvDate: TextView, view: ConstraintLayout, position: Int) {
        if (0 < position) {
            if (GeneralFunctions.changeDateFormat(
                    mList[position].createdAt.toString(),
                    Constants.DATE_FORMAT_SERVER,
                    DATE_FORMAT_DISPLAY
                ).equals(
                    GeneralFunctions.changeDateFormat(
                        mList[position - 1].createdAt.toString(),
                        Constants.DATE_FORMAT_SERVER,
                        DATE_FORMAT_DISPLAY
                    ), true
                )
            )
                view.visibility = View.GONE
            else {
                view.visibility = View.VISIBLE
                val calender = Calendar.getInstance()
                if (GeneralFunctions.changeDateFormat(
                        mList[position].createdAt.toString(),
                        Constants.DATE_FORMAT_SERVER,
                        DATE_FORMAT_DISPLAY
                    ) == sdfTime.format(calender.time)
                ) tvDate.text = mContext?.getString(R.string.st_today)
                else
                    tvDate.text = GeneralFunctions.changeDateFormat(
                        mList[position].createdAt.toString(),
                        Constants.DATE_FORMAT_SERVER,
                        DATE_FORMAT_DISPLAY
                    )

            }
        } else {
            view.visibility = View.VISIBLE
            val calender = Calendar.getInstance()
            if (GeneralFunctions.changeDateFormat(
                    mList[position].createdAt.toString(),
                    Constants.DATE_FORMAT_SERVER,
                    DATE_FORMAT_DISPLAY
                ) == sdfTime.format(calender.time)
            ) tvDate.text = mContext?.getString(R.string.st_today)
            else
                tvDate.text = GeneralFunctions.changeDateFormat(
                    mList[position].createdAt.toString(),
                    Constants.DATE_FORMAT_SERVER,
                    DATE_FORMAT_DISPLAY
                )
        }
    }


}