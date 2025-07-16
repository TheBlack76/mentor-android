package com.mentor.application.views.vendor.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.R
import com.mentor.application.databinding.LayoutItemInstantBookingBinding
import com.mentor.application.databinding.LayoutNewRequestItemBinding
import com.mentor.application.databinding.RowLoadMoreBinding
import com.mentor.application.repository.models.BookingRequest
import com.mentor.application.repository.models.enumValues.BookingStatus
import com.mentor.application.repository.models.enumValues.BookingType
import com.mentor.application.utils.Constants
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.views.vendor.fragments.NewRequestFragment.Companion.NEW_BOOKINGS
import com.mentor.application.views.vendor.interfaces.NewRequestInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject


@FragmentScoped
class NewRequestAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    val mListener: NewRequestInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ROW_TYPE_LIST = 1
        private const val ROW_TYPE_INSTANT = 2
        private const val ROW_TYPE_LOAD_MORE = 3
        const val LIMIT = 10

    }

    private var mList = mutableListOf<BookingRequest>()
    private var mBookingType = NEW_BOOKINGS
    private var isLoadMoreData = false
    private val timersMap = mutableMapOf<Int, CountDownTimer>()  // Store timers by position



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ROW_TYPE_LIST -> {
                ListViewHolder(
                    LayoutNewRequestItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            ROW_TYPE_INSTANT -> {
                InstantListViewHolder(
                    LayoutItemInstantBookingBinding.inflate(
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

    fun updateData(list: List<BookingRequest>, page: Int = 0, bookingType: String) {
        if (page == 0) {
            mList.clear()
        }
        mList.addAll(list)
        isLoadMoreData = list.size >= LIMIT
        mBookingType = bookingType
//        timersMap.map {
//            it.value.cancel()
//        }
//        timersMap.clear()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            mList.size -> ROW_TYPE_LOAD_MORE
            else -> if (mList[position].bookingType == BookingType.INSTANT.value) ROW_TYPE_INSTANT else ROW_TYPE_LIST
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        timersMap[holder.absoluteAdapterPosition]?.cancel()  // Cancel timer when item is recycled
        timersMap.remove(holder.absoluteAdapterPosition)  // Remove from map
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ROW_TYPE_LIST -> {
                (holder as ListViewHolder)
                    .bindListView(mList[position])
            }

            ROW_TYPE_INSTANT -> {
                (holder as InstantListViewHolder)
                    .bindListView(mList[position])
            }

            else -> {
                (holder as LoadMoreViewHolder)
                    .bindData(position)
            }
        }
    }


    private inner class ListViewHolder(val binding: LayoutNewRequestItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindListView(
            data: BookingRequest
        ) {
            binding.tvBookingId.text = data.bookingId
            binding.tvName.text = data.customerId.fullName
            binding.tvService.text = data.subProfessionId.subProfession
            binding.sdvImage.setImageURI(GeneralFunctions.getUserImage(data.customerId.image))

            val date =
                GeneralFunctions.changeDateFormat(
                    data.date, Constants.DATE_FORMAT_SERVER, Constants.DATE_FORMAT_DISPLAY
                ) + " (" +
                        GeneralFunctions.changeDateFormat(
                            data.startTime,
                            Constants.DATE_SERVER_TIME,
                            Constants.DATE_DISPLAY_TIME
                        ) + ")"

            binding.tvDate.text = date

            when (data.durationType) {
                "30" -> binding.tvPrice.text = "$${data.bookingAmount}/30min"
                "60" -> binding.tvPrice.text = "$${data.bookingAmount}/60min"
                "90" -> binding.tvPrice.text = "$${data.bookingAmount}/90min"
            }

            if (mBookingType == NEW_BOOKINGS) {
                binding.btnAccept.visibility = View.VISIBLE
                binding.btnReject.visibility = View.VISIBLE
            } else {
                binding.btnAccept.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
            }

            when (data.status) {
                BookingStatus.CANCELLED.value -> {
                    binding.tvBookingStatus.visibility = View.VISIBLE
                    binding.tvBookingStatus.text = mContext?.getString(R.string.st_cancelled)
                    binding.tvBookingStatus.setTextColor(
                        ContextCompat.getColorStateList(
                            mContext!!, R.color.colorRed
                        )
                    )

                    binding.tvBookingStatus.backgroundTintList = ContextCompat.getColorStateList(
                        mContext, R.color.colorRedDisable
                    )
                }

                BookingStatus.REJECTED.value -> {
                    binding.tvBookingStatus.visibility = View.VISIBLE
                    binding.tvBookingStatus.text = mContext?.getString(R.string.st_rejected)
                    binding.tvBookingStatus.setTextColor(
                        ContextCompat.getColorStateList(
                            mContext!!, R.color.colorRed
                        )
                    )
                    binding.tvBookingStatus.backgroundTintList = ContextCompat.getColorStateList(
                        mContext, R.color.colorRedDisable
                    )
                }

                BookingStatus.COMPLETED.value -> {
                    binding.tvBookingStatus.visibility = View.VISIBLE
                    binding.tvBookingStatus.text = mContext?.getString(R.string.st_completed)
                    binding.tvBookingStatus.setTextColor(
                        ContextCompat.getColorStateList(
                            mContext!!, R.color.colorGreen
                        )
                    )
                    binding.tvBookingStatus.backgroundTintList = ContextCompat.getColorStateList(
                        mContext, R.color.colorGreenDisable
                    )
                }

                BookingStatus.ONGOING.value -> {
                    binding.tvBookingStatus.visibility = View.VISIBLE
                    binding.tvBookingStatus.text = mContext?.getString(R.string.st_ongoing)
                    binding.tvBookingStatus.setTextColor(
                        ContextCompat.getColorStateList(
                            mContext!!, R.color.colorGreen
                        )
                    )
                    binding.tvBookingStatus.backgroundTintList = ContextCompat.getColorStateList(
                        mContext, R.color.colorGreenDisable
                    )
                }

                else -> {
                    binding.tvBookingStatus.visibility = View.GONE
                }
            }

            itemView.setOnClickListener {
                mListener.onItemClick(data._id, data.requestSentTime, data.requestDurationTime)
            }

            binding.btnAccept.setOnClickListener {
                mListener.onAccept(data)
            }

            binding.btnReject.setOnClickListener {
                mListener.onReject(data)

            }

        }
    }

    private inner class InstantListViewHolder(val binding: LayoutItemInstantBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            data: BookingRequest
        ) {

            binding.tvBookingId.text = data.bookingId
            binding.tvName.text = data.customerId.fullName
            binding.tvService.text = data.subProfessionId.subProfession
            binding.sdvImage.setImageURI(GeneralFunctions.getUserImage(data.customerId.image))

            when (data.durationType) {
                "30" -> binding.tvPrice.text = "$${data.bookingAmount}/30min"
                "60" -> binding.tvPrice.text = "$${data.bookingAmount}/60min"
                "90" -> binding.tvPrice.text = "$${data.bookingAmount}/90min"
            }

            if (mBookingType == NEW_BOOKINGS) {
                binding.acceptContainer.visibility = View.VISIBLE
                binding.btnReject.visibility = View.VISIBLE
                binding.btnCustom.visibility = View.VISIBLE
            } else {
                binding.acceptContainer.visibility = View.GONE
                binding.btnReject.visibility = View.GONE
                binding.btnCustom.visibility = View.GONE
            }

            when (data.status) {
                BookingStatus.CANCELLED.value -> {
                    binding.tvBookingStatus.visibility = View.VISIBLE
                    binding.tvBookingStatus.text = mContext?.getString(R.string.st_cancelled)
                    binding.tvBookingStatus.setTextColor(
                        ContextCompat.getColorStateList(
                            mContext!!, R.color.colorRed
                        )
                    )

                    binding.tvBookingStatus.backgroundTintList = ContextCompat.getColorStateList(
                        mContext, R.color.colorRedDisable
                    )
                }

                BookingStatus.REJECTED.value -> {
                    binding.tvBookingStatus.visibility = View.VISIBLE
                    binding.tvBookingStatus.text = mContext?.getString(R.string.st_rejected)
                    binding.tvBookingStatus.setTextColor(
                        ContextCompat.getColorStateList(
                            mContext!!, R.color.colorRed
                        )
                    )
                    binding.tvBookingStatus.backgroundTintList = ContextCompat.getColorStateList(
                        mContext, R.color.colorRedDisable
                    )
                }

                BookingStatus.COMPLETED.value -> {
                    binding.tvBookingStatus.visibility = View.VISIBLE
                    binding.tvBookingStatus.text = mContext?.getString(R.string.st_completed)
                    binding.tvBookingStatus.setTextColor(
                        ContextCompat.getColorStateList(
                            mContext!!, R.color.colorGreen
                        )
                    )
                    binding.tvBookingStatus.backgroundTintList = ContextCompat.getColorStateList(
                        mContext, R.color.colorGreenDisable
                    )
                }

                BookingStatus.ONGOING.value -> {
                    binding.tvBookingStatus.visibility = View.VISIBLE
                    binding.tvBookingStatus.text = mContext?.getString(R.string.st_ongoing)
                    binding.tvBookingStatus.setTextColor(
                        ContextCompat.getColorStateList(
                            mContext!!, R.color.colorGreen
                        )
                    )
                    binding.tvBookingStatus.backgroundTintList = ContextCompat.getColorStateList(
                        mContext, R.color.colorGreenDisable
                    )
                }

                else -> {
                    binding.tvBookingStatus.visibility = View.GONE
                }
            }

            itemView.setOnClickListener {
                mListener.onItemClick(data._id,
                    data.requestSentTime,data.requestDurationTime)
            }

            binding.acceptContainer.setOnClickListener {
                mListener.onAccept(data)
            }

            binding.btnCustom.setOnClickListener {
                mListener.onCustomOffered(data)
            }

            binding.btnReject.setOnClickListener {
                mListener.onReject(data)

            }


            val createdAtMillis = getLocalMillisFromISOString(data.requestSentTime)
            val endTimeMillis = createdAtMillis + (data.requestDurationTime * 1000)
            val remainingTime = (endTimeMillis - System.currentTimeMillis()).coerceAtLeast(0)

            // **Cancel previous timer if exists**
            timersMap[absoluteAdapterPosition]?.cancel()

            binding.progressBar.max=data.requestDurationTime
            if (remainingTime > 0) {
                val timer = object : CountDownTimer(remainingTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val secondsRemaining = millisUntilFinished / 1000  // Convert to seconds
                        Log.e("durationLeft", "onTick: "+secondsRemaining )
                        binding.buttonText.text="Accept in (${secondsRemaining}s)"
                        binding.progressBar.progress = secondsRemaining.toInt() // Update progress in seconds
                    }

                    override fun onFinish() {
                        binding.progressBar.progress = 0
                        removeItem()
                    }
                }
                timersMap[absoluteAdapterPosition] = timer  // Store the timer
                timer.start()
            } else {
                binding.progressBar.progress = 0
                removeItem()
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

    private fun removeItem() {
        if (mBookingType == NEW_BOOKINGS){
            mListener.onRefresh()
        }
    }
    fun getLocalMillisFromISOString(isoString: String): Long {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC") // Parse in UTC
            val date = formatter.parse(isoString)
            date?.time ?: 0L // This is already in UTC millis; local time is automatic when displaying
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }



}