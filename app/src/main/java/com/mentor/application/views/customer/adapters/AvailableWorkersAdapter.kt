package com.mentor.application.views.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.databinding.LayoutItemAvailableWorkerItemBinding
import com.mentor.application.databinding.RowLoadMoreBinding
import com.mentor.application.repository.models.User
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.views.customer.interfaces.WorkersInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject


@FragmentScoped
class AvailableWorkersAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    val mListener: WorkersInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ROW_TYPE_LIST = 1
        private const val ROW_TYPE_LOAD_MORE = 2

        const val LIMIT = 10

    }

    private var mList = mutableListOf<User>()
    private var isLoadMoreData = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ROW_TYPE_LIST -> {

                ListViewHolder(
                    LayoutItemAvailableWorkerItemBinding.inflate(
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

    fun updateData(list: List<User>, page: Int = 0) {
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


    private inner class ListViewHolder(val binding: LayoutItemAvailableWorkerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            user : User
        ) {

            binding.sdvVendorImage.setImageURI(GeneralFunctions.getUserImage(mList[absoluteAdapterPosition].image))
            binding.tvName.text = mList[absoluteAdapterPosition].fullName
            binding.tvRatting.text=mList[absoluteAdapterPosition].averageStars.toString()

            val professionNames = user.professions?.map { it.profession }
            binding.tvTagline.text = professionNames?.joinToString(" || ")


            binding.tv30MinPrice.text = GeneralFunctions.getPrice(
                user.halfHourlyRate.toString(), "30min,", mContext!!
            )

            binding.tv60MinPrice.text = GeneralFunctions.getPrice(
                user.hourlyRate.toString(), "60min,", mContext
            )

            binding.tv90MinPrice.text = GeneralFunctions.getPrice(
                user.oneAndHalfHourlyRate.toString(), "90min", mContext
            )

            itemView.setOnClickListener {
                mListener.onItemClick(mList[absoluteAdapterPosition])
            }


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