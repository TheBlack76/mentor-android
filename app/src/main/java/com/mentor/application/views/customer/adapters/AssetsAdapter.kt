package com.mentor.application.views.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.databinding.FragmentImageBinding
import com.mentor.application.databinding.RowLoadMoreBinding
import com.mentor.application.views.customer.interfaces.AssetsInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject


@FragmentScoped
class AssetsAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    var mListener: AssetsInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_LIST = 0
        const val VIEW_MORE = 1
        const val LIMIT = 10
    }


//    private var mList = mutableListOf<Media>()
    private var isMedia = true
    private var isLoadMore = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_LIST -> {
                ListViewHolder(
                    FragmentImageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                MoreViewHolder(
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
        return 10
    }

//    fun updateData(list: List<Media>, page: Int = 0) {
//        if (page == 0) {
//            mList.clear()
//        }
//        mList.addAll(list)
//        notifyDataSetChanged()
//    }

//    override fun getItemViewType(position: Int): Int {
//        return when (position) {
//            mList.size -> {
//                VIEW_MORE
//            }
//            else -> {
//                VIEW_LIST
//            }
//        }
//    }


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


    private inner class ListViewHolder(val binding: FragmentImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {

        }
    }

    private inner class MoreViewHolder(val binding: RowLoadMoreBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindMoreView(
            position: Int
        ) {
            if (isLoadMore) {
                binding.progressBar.visibility = View.VISIBLE
                mListener.onLoadMore()
            } else {
                binding.progressBar.visibility = View.GONE

            }

        }
    }

}