package com.mentor.application.views.vendor.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mentor.application.R
import com.mentor.application.databinding.LayoutPersonalisationPastWorkBinding
import com.mentor.application.databinding.LayoutUploadItemBinding
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.views.vendor.interfaces.EnterDetailInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import java.io.File
import javax.inject.Inject


@FragmentScoped
class UploadPastWorkAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    var mListener: EnterDetailInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_LIST = 0
        const val VIEW_ADD = 1
    }


    private var mList = mutableListOf<String>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_LIST -> {
                ListViewHolder(
                    LayoutPersonalisationPastWorkBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> {
                MoreViewHolder(
                    LayoutUploadItemBinding.inflate(
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

    fun updateData(list: List<String>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            mList.size -> {
                VIEW_ADD
            }

            else -> {
                VIEW_LIST
            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when {
            VIEW_LIST == getItemViewType(position) -> {
                (holder as ListViewHolder).bindListView(position)
            }

            else -> {
                (holder as MoreViewHolder).bindMoreView(position)
            }
        }
    }


    private inner class ListViewHolder(val binding: LayoutPersonalisationPastWorkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {
            // If the file exist with same name in local show from local else from server
            val mImageFile =
                GeneralFunctions.getLocalMediaFile(mContext!!, File(mList[absoluteAdapterPosition]).name)

            val path = if (mImageFile.exists()) {
                GeneralFunctions.getLocalImageFile(mImageFile)
            } else {
                GeneralFunctions.getImage(mList[absoluteAdapterPosition])
            }

            // Use Glide to load the image into the ImageView
            Glide.with(mContext!!)
                .load(path) // Specify the image URL
                .placeholder(R.color.colorPlaceHolder) // Optional: Placeholder image
                .into(binding.sdvDocument)

            if (mList[absoluteAdapterPosition].endsWith(".mp4")) {
                binding.ivPlayBtn.visibility = View.VISIBLE
            } else {
                binding.ivPlayBtn.visibility = View.GONE
            }

            itemView.setOnClickListener {
                mListener.onWorkClick(mList[absoluteAdapterPosition])
            }

            binding.ivDelete.setOnClickListener {
                mListener.onDeletePastWork(absoluteAdapterPosition)
            }

        }
    }

    private inner class MoreViewHolder(val binding: LayoutUploadItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindMoreView(
            position: Int
        ) {
            itemView.setOnClickListener {
                mListener.onAddWorkClick()
            }


        }
    }

}