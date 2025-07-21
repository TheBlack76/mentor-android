package com.sportex.app.appCricket.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.mentor.application.R
import com.mentor.application.databinding.LayoutProfessionSelcetItemBinding
import com.mentor.application.repository.models.Profession
import com.mentor.application.views.vendor.adapters.ProfessionSubCategoryAdapter
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
class ProfessionCheckListAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var mList = mutableListOf<Profession>()


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

    fun updateData(list: List<Profession>) {
        mList.clear()
        mList.addAll(list)

        mList.forEach { profession ->
            profession.isOpen = profession.subProfessions?.any { it.isChecked } == true
        }

        notifyDataSetChanged()
    }

    fun getSelectedList(): List<Profession> {
        return mList
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ListViewHolder).bindListView(position)
    }

    private inner class ListViewHolder(val binding: LayoutProfessionSelcetItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {

            binding.checkbox.text = mList[absoluteAdapterPosition].profession

            val adapter = ProfessionSubCategoryAdapter(
                mContext!!,
                mList[absoluteAdapterPosition].subProfessions!!,
                true
            )
            val layoutManager = FlexboxLayoutManager(mContext)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.FLEX_START
            layoutManager.alignItems = AlignItems.CENTER
            binding.rvList.setLayoutManager(layoutManager)
            binding.rvList.adapter = adapter

            if (mList[absoluteAdapterPosition].isOpen) {
                binding.rvList.visibility = View.VISIBLE
                binding.checkbox.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_upword_arrow,
                    0
                );

            } else {
                binding.rvList.visibility = View.GONE
                binding.checkbox.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_down_arrow,
                    0
                );
            }


            // Set check listener
            binding.checkbox.setOnClickListener {
                mList[absoluteAdapterPosition].isOpen = !mList[absoluteAdapterPosition].isOpen
                notifyItemChanged(absoluteAdapterPosition)
            }

        }
    }

}