package com.mentor.application.views.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.R
import com.mentor.application.databinding.LayoutTimeSlotItemBinding
import com.mentor.application.repository.models.TimeSlot
import com.mentor.application.utils.Constants
import com.mentor.application.utils.GeneralFunctions
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import java.sql.Time
import javax.inject.Inject


class TimeSlotsAdapter (val mContext: Fragment
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mList = mutableListOf<TimeSlot>()
    private var mSlot = TimeSlot()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListViewHolder(
            LayoutTimeSlotItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun updateData(list: List<TimeSlot>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    fun onTimeSelected(timeSlot:TimeSlot){
        mSlot = timeSlot
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ListViewHolder).bindListView(mList[position])

    }

    private inner class ListViewHolder(val binding: LayoutTimeSlotItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            data: TimeSlot
        ) {
            "${
                GeneralFunctions.changeDateFormat(
                    data.startTime, Constants.DATE_SERVER_TIME,
                    Constants.DATE_DISPLAY_TIME
                )
            } - ${
                GeneralFunctions.changeDateFormat(
                    data.endTime, Constants.DATE_SERVER_TIME,
                    Constants.DATE_DISPLAY_TIME
                )
            }".also { binding.tvSlotsTime.text = it }

            if (mList[absoluteAdapterPosition] == mSlot) {
                binding.tvSlotsTime.setTextColor(ContextCompat.getColor(mContext.requireContext()!!, R.color.colorWhite))
                binding.tvSlotsTime.background = ContextCompat.getDrawable(
                    mContext.requireContext()!!,
                    R.drawable.drawable_color_medium_rounded_corners_solid
                )
            } else {
                binding.tvSlotsTime.setTextColor(
                    ContextCompat.getColor(
                        mContext.requireContext()!!,
                        R.color.colorPrimaryText
                    )
                )
                binding.tvSlotsTime.background =
                    ContextCompat.getDrawable(mContext.requireContext()!!, R.drawable.drawable_small_round_stroke_normal)
            }

            itemView.setOnClickListener {
                mSlot = mList[absoluteAdapterPosition]
                (mContext as TimeSlotsInterFace).onItemClick(mSlot)
            }

        }
    }

}

interface TimeSlotsInterFace{
    fun onItemClick(time:TimeSlot)
}