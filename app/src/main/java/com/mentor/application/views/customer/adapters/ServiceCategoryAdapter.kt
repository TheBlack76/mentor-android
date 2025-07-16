package com.mentor.application.views.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.databinding.ItemInstantBookingBinding
import com.mentor.application.databinding.LayoutItemServiceCategoryBinding
import com.mentor.application.databinding.LayoutNotificationItemBinding
import com.mentor.application.databinding.RowLoadMoreBinding
import com.mentor.application.repository.models.Profession

import com.mentor.application.views.customer.adapters.NotificationAdapter.LoadMoreViewHolder
import com.mentor.application.views.customer.interfaces.HomeInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject


@FragmentScoped
class ServiceCategoryAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    var mListener: HomeInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ROW_TYPE_LIST = 1
        private const val ROW_TYPE_INSTANT_BOOKING = 2

    }

    private var mList = mutableListOf<Profession>()
    private var isInstantBooking = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            ROW_TYPE_LIST -> {
                ListViewHolder(
                    LayoutItemServiceCategoryBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> {
                InstantBookingViewHolder(
                    ItemInstantBookingBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isInstantBooking) mList.size + 1 else mList.size
    }

    fun updateData(list: List<Profession>, isInstant: Boolean = true) {
        mList.clear()
        mList.addAll(list)
        isInstantBooking = isInstant
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isInstantBooking) {
            when (position) {
                0 -> ROW_TYPE_INSTANT_BOOKING
                else -> ROW_TYPE_LIST
            }
        } else {
            ROW_TYPE_LIST


        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ROW_TYPE_LIST -> {
                (holder as ListViewHolder)
                    .bindListView(if (isInstantBooking) position - 1 else position)
            }

            else -> {
                (holder as InstantBookingViewHolder)
                    .bindData(position)
            }
        }

    }


    private inner class ListViewHolder(val binding: LayoutItemServiceCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {

            binding.tvName.text = mList[position].profession

            // Set adapter
            val mServiceSubCategoryAdapter = mList[position].subProfessions?.let {
                ServiceSubCategoryAdapter(
                    mContext,
                    it,
                    mList[position],
                    mListener
                )
            }
            binding.recyclerView.adapter = mServiceSubCategoryAdapter

        }
    }

    inner class InstantBookingViewHolder(val binding: ItemInstantBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(position: Int) {

            if (isInstantBooking) binding.clItemView.visibility = View.VISIBLE else View.GONE

            binding.btnSubmit.setOnClickListener {
                mListener.onInstantClick()
            }

        }
    }

}