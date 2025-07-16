package com.mentor.application.views.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.R
import com.mentor.application.databinding.LayoutUpcomingBookingItemBinding
import com.mentor.application.databinding.RowLoadMoreBinding
import com.mentor.application.repository.models.Booking
import com.mentor.application.repository.models.enumValues.BookingStatus
import com.mentor.application.repository.models.enumValues.BookingType
import com.mentor.application.utils.Constants
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.views.customer.interfaces.BookingsInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject


@FragmentScoped
class UpcomingBookingAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    var mListener: BookingsInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ROW_TYPE_LIST = 1
        private const val ROW_TYPE_LOAD_MORE = 2

        const val LIMIT = 10

    }

    private var mList = mutableListOf<Booking>()
    private var isLoadMoreData = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ROW_TYPE_LIST -> {
                ListViewHolder(
                    LayoutUpcomingBookingItemBinding.inflate(
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

    fun updateData(list: List<Booking>, page: Int = 0) {
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


    private inner class ListViewHolder(val binding: LayoutUpcomingBookingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            booking: Booking
        ) {

            binding.tvBookingId.text = booking.bookingId
            binding.sdvImage.setImageURI(
                booking.subProfessionId.image
            )
            binding.tvName.text = booking.subProfessionId.subProfession

            if (booking.bookingType == BookingType.INSTANT.value) {
                binding.ivInstant.visibility = View.VISIBLE
                binding.tvDate.visibility = View.GONE
                binding.clItemView.backgroundTintList=ContextCompat.getColorStateList(
                    mContext!!,R.color.colorWhite
                )
            } else {
                binding.ivInstant.visibility = View.GONE
                binding.tvDate.visibility = View.VISIBLE
                binding.clItemView.backgroundTintList=ContextCompat.getColorStateList(
                    mContext!!,R.color.colorWhite
                )
            }

            val date =
                GeneralFunctions.changeDateFormat(
                    booking.date, Constants.DATE_FORMAT_SERVER, Constants.DATE_FORMAT_DISPLAY
                ) + " (" +
                        GeneralFunctions.changeDateFormat(
                            booking.startTime,
                            Constants.DATE_SERVER_TIME,
                            Constants.DATE_DISPLAY_TIME
                        ) + ")"

            binding.tvDate.text = date

            when (booking.durationType) {
                "60" -> binding.tvPrice.text = "$${booking.totalAmount}/60min"
                "30" -> binding.tvPrice.text = "$${booking.totalAmount}/30min"
                "90" -> binding.tvPrice.text = "$${booking.totalAmount}/90min"
            }

            when (booking.status) {
                BookingStatus.CANCELLED.value -> {
                    binding.tvCancelBooking.text = mContext.getString(R.string.st_cancelled)
                    binding.tvCancelBooking.setTextColor(
                        ContextCompat.getColorStateList(
                            mContext, R.color.colorRed
                        )
                    )
                    binding.tvCancelBooking.backgroundTintList = ContextCompat.getColorStateList(
                        mContext, R.color.colorRedDisable
                    )
                }

                BookingStatus.REJECTED.value -> {
                    binding.tvCancelBooking.text = mContext.getString(R.string.st_rejected)
                    binding.tvCancelBooking.setTextColor(
                        ContextCompat.getColorStateList(
                            mContext!!, R.color.colorRed
                        )
                    )
                    binding.tvCancelBooking.backgroundTintList = ContextCompat.getColorStateList(
                        mContext, R.color.colorRedDisable
                    )

                }

                BookingStatus.COMPLETED.value -> {
                    binding.tvCancelBooking.text = mContext?.getString(R.string.st_completed)
                    binding.tvCancelBooking.setTextColor(
                        ContextCompat.getColorStateList(
                            mContext!!, R.color.colorGreen
                        )
                    )
                    binding.tvCancelBooking.backgroundTintList = ContextCompat.getColorStateList(
                        mContext, R.color.colorGreenDisable
                    )

                }

                BookingStatus.ACCEPTED.value -> {
                    binding.tvCancelBooking.text = mContext?.getString(R.string.st_accepted)
                    binding.tvCancelBooking.setTextColor(
                        ContextCompat.getColorStateList(
                            mContext!!, R.color.colorGreen
                        )
                    )
                    binding.tvCancelBooking.backgroundTintList = ContextCompat.getColorStateList(
                        mContext, R.color.colorGreenDisable
                    )

                }

                BookingStatus.ONGOING.value -> {
                    binding.tvCancelBooking.text = mContext?.getString(R.string.st_ongoing)
                    binding.tvCancelBooking.setTextColor(
                        ContextCompat.getColorStateList(
                            mContext!!, R.color.colorGreen
                        )
                    )
                    binding.tvCancelBooking.backgroundTintList = ContextCompat.getColorStateList(
                        mContext, R.color.colorGreenDisable
                    )

                }

                BookingStatus.REQUESTED.value -> {
                    binding.tvCancelBooking.text = mContext?.getString(R.string.st_requested)
                    binding.tvCancelBooking.setTextColor(
                        ContextCompat.getColorStateList(
                            mContext!!, R.color.colorGrayBg
                        )
                    )
                    binding.tvCancelBooking.backgroundTintList = ContextCompat.getColorStateList(
                        mContext, R.color.colorDisableGrey
                    )
                }

            }

            itemView.setOnClickListener {
                mListener.onItemClick(mList[absoluteAdapterPosition]._id)
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