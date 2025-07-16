package com.sportex.app.appCricket.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.databinding.LayoutProfessionSelcetItemBinding
import com.mentor.application.repository.models.Profession
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
class ProfessionCheckListAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var mList = mutableListOf<Profession>()
    private var mSelectedItem = mutableListOf<Profession>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListViewHolder(
            LayoutProfessionSelcetItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun updateData(list: List<Profession>, selectedList: List<Profession>) {
        mList.clear()
        mSelectedItem.clear()
        mSelectedItem.addAll(selectedList)
        mList.addAll(list)
        notifyDataSetChanged()

    }

    fun getSelectedList(): List<Profession> {
        return mSelectedItem
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ListViewHolder).bindListView(position)
    }

    private inner class ListViewHolder(val binding: LayoutProfessionSelcetItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {

            binding.checkbox.text=mList[absoluteAdapterPosition].profession

            binding.checkbox.isChecked = mSelectedItem.contains(mList[absoluteAdapterPosition])

            // Set check listener
            binding.checkbox.setOnClickListener {
                if (binding.checkbox.isChecked) {
                    mSelectedItem.add(mList[position])
                } else {
                    mSelectedItem.remove(mList[position])
                }
            }

        }
    }

}