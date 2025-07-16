package com.mentor.application.views.vendor.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.databinding.LayoutDocumentItemBinding
import com.mentor.application.databinding.LayoutUploadItemBinding
import com.mentor.application.views.vendor.interfaces.EnterDetailInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import java.io.File
import javax.inject.Inject


@FragmentScoped
class UploadCertificateAdapter @Inject constructor(
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
                    LayoutDocumentItemBinding.inflate(
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


    private inner class ListViewHolder(val binding: LayoutDocumentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {

            binding.tvName.text=File(mList[absoluteAdapterPosition]).name

            itemView.setOnClickListener {
                mListener.onDocumentClick(mList[absoluteAdapterPosition])
            }

            binding.ivDelete.setOnClickListener {
                mListener.onDeleteCertificate(absoluteAdapterPosition)
            }

        }
    }

    private inner class MoreViewHolder(val binding: LayoutUploadItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindMoreView(
            position: Int
        ) {
            itemView.setOnClickListener {
                mListener.onAddCertificateClick()
            }


        }
    }

}