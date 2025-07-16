package com.mentor.application.views.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mentor.application.R
import com.mentor.application.databinding.LayoutItemPreviousWorkBinding
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.views.vendor.interfaces.VendorProfileInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import java.io.File
import javax.inject.Inject


@FragmentScoped
class PreviousWorkAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    var mListener: VendorProfileInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mList = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListViewHolder(
            LayoutItemPreviousWorkBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun updateData(list: List<String>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ListViewHolder).bindListView(position)

    }


    private inner class ListViewHolder(val binding: LayoutItemPreviousWorkBinding) :
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
            Glide.with(mContext)
                .load(path) // Specify the image URL
                .placeholder(R.color.colorPlaceHolder) // Optional: Placeholder image
                .into(binding.sdvDocument)

            if (mList[absoluteAdapterPosition].endsWith(".mp4")) {
                binding.ivPlayBtn.visibility = View.VISIBLE
            } else {
                binding.ivPlayBtn.visibility = View.GONE
            }

            itemView.setOnClickListener {
                mListener.onPastWorkClick(mList[absoluteAdapterPosition])
            }


        }
    }

}