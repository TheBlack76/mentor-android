package com.mentor.application.views.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.R
import com.mentor.application.databinding.LayoutNotificationItemBinding
import com.mentor.application.databinding.RowLoadMoreBinding
import com.mentor.application.repository.models.NotificationListing
import com.mentor.application.utils.Constants
import com.mentor.application.utils.Constants.DATE_DISPLAY_TIME
import com.mentor.application.utils.Constants.DATE_FORMAT_DISPLAY
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.views.customer.interfaces.NotificationInterface

import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


@FragmentScoped
class NotificationAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    var mListener: NotificationInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ROW_TYPE_LIST = 1
        private const val ROW_TYPE_LOAD_MORE = 2
        const val LIMIT = 10

    }

    private val sdfTime: SimpleDateFormat by lazy {
        SimpleDateFormat(
            DATE_FORMAT_DISPLAY,
            Locale.US
        )
    }

    private var mList = mutableListOf<NotificationListing>()
    private var isLoadMoreData = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            ROW_TYPE_LIST -> {
                ListViewHolder(
                    LayoutNotificationItemBinding.inflate(
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

    fun updateData(list: List<NotificationListing>, page: Int = 0) {
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
                    .bindListView(position)
            }

            else -> {
                (holder as LoadMoreViewHolder)
                    .bindData(position)
            }
        }
    }


    private inner class ListViewHolder(val binding: LayoutNotificationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {
            binding.tvTitle.text = mList[absoluteAdapterPosition].title
            binding.tvMessage.text = mList[absoluteAdapterPosition].description

            setDate(binding.tvDate,position)


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

    private fun setDate(tvDate: TextView, position: Int) {
        val calender = Calendar.getInstance()
        if (GeneralFunctions.changeDateFormat(
                mList[position].createdAt,
                Constants.DATE_FORMAT_SERVER,
                DATE_FORMAT_DISPLAY
            ) == sdfTime.format(calender.time)
        ) tvDate.text = buildString {
            append(mContext?.getString(R.string.st_today))
            append(" ")
            append(
                GeneralFunctions.changeUtcToLocal(
                    mList[position].createdAt,
                    Constants.DATE_FORMAT_SERVER,
                    DATE_DISPLAY_TIME
                )
            )
        }
        else
            tvDate.text = GeneralFunctions.changeDateFormat(
                mList[position].createdAt.toString(),
                Constants.DATE_FORMAT_SERVER,
                DATE_FORMAT_DISPLAY
            )

    }

}