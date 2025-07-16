package com.mentor.application.views.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.databinding.LayoutReviewsItemBinding
import com.mentor.application.databinding.RowLoadMoreBinding
import com.mentor.application.repository.models.ReviewData
import com.mentor.application.utils.Constants
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.views.customer.interfaces.ReviewsInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject


@FragmentScoped
class ReviewsAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    var mListener: ReviewsInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ROW_TYPE_LIST = 1
        private const val ROW_TYPE_LOAD_MORE = 2

        const val LIMIT = 10

    }

    private var mList = mutableListOf<ReviewData>()
    private var isLoadMoreData = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ROW_TYPE_LIST -> {
                ListViewHolder(
                    LayoutReviewsItemBinding.inflate(
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

    fun updateData(list: List<ReviewData>, page: Int = 0) {
        if (page == 0) {
            mList.clear()
        }
        mList.addAll(list)
        isLoadMoreData = if (list.size < LIMIT) false else true
        notifyDataSetChanged()

    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            mList.size -> ROW_TYPE_LOAD_MORE
            else -> ROW_TYPE_LIST
        }
    }

    override fun getItemCount(): Int {
        return mList.size + 1
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ROW_TYPE_LIST -> {
                (holder as ListViewHolder)
                    .bindListView(mList[position])
            }

            else -> {
                (holder as LoadMoreViewHolder)
                    .bindData(position)
            }
        }

    }

    private inner class ListViewHolder(val binding: LayoutReviewsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            data: ReviewData
        ) {

            binding.tvName.text = data.customerId.fullName
            binding.tvDate.text = GeneralFunctions.changeUtcToLocal(
                data.createdAt,
                Constants.DATE_FORMAT_SERVER, Constants.DATE_FORMAT_DISPLAY
            )

            binding.ratting.rating = data.star.toFloat()
            binding.tvDescription.text = data.message

        }
    }

    inner class LoadMoreViewHolder(val binding: RowLoadMoreBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(position: Int) {

            if (isLoadMoreData) {
                binding.progressBar.visibility = View.VISIBLE
                mListener.onLoadMore()
            } else {
                binding.progressBar.visibility = View.GONE

            }

        }
    }


}