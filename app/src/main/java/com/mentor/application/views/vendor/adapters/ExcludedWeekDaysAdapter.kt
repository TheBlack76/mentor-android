package com.mentor.application.views.vendor.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.databinding.ItemSlotsGeneratedItemBinding
import com.mentor.application.views.vendor.interfaces.EditAvailabilityInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject


@FragmentScoped
class ExcludedWeekDaysAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    var mListener: EditAvailabilityInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mList= mutableListOf<String>()

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

    fun updateData(value:String){
        if (!mList.contains(value)){
            mList.add(value)
        }
        notifyDataSetChanged()
    }

    fun getList():List<String>{
        return mList
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ListViewHolder).bindListView(position)
    }


    private inner class ListViewHolder(val binding: ItemSlotsGeneratedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {

            binding.tvTime.text=mList[absoluteAdapterPosition]

            binding.ivCancel.setOnClickListener {
                mList.removeAt(absoluteAdapterPosition)
                notifyDataSetChanged()
            }

        }
    }

}