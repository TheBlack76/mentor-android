package com.mentor.application.views.vendor.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.databinding.ItemSlotsGeneratedItemBinding
import com.mentor.application.repository.models.TimeSlot
import com.mentor.application.utils.Constants
import com.mentor.application.utils.GeneralFunctions
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject


@FragmentScoped
class AvailableSlotsAdapter @Inject constructor(
    @ActivityContext val mContext: Context?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mList= mutableListOf<TimeSlot>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListViewHolder(
            ItemSlotsGeneratedItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun updateData(list:List<TimeSlot>){
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ListViewHolder).bindListView(position)
    }


    private inner class ListViewHolder(val binding: ItemSlotsGeneratedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {

            "${
                GeneralFunctions.changeDateFormat(
                    mList[absoluteAdapterPosition].startTime, Constants.DATE_SERVER_TIME,
                    Constants.DATE_DISPLAY_TIME
                )
            } - ${
                GeneralFunctions.changeDateFormat(
                    mList[absoluteAdapterPosition].endTime, Constants.DATE_SERVER_TIME,
                    Constants.DATE_DISPLAY_TIME
                )
            }".also { binding.tvTime.text = it }


            binding.ivCancel.visibility=View.GONE

        }
    }

}