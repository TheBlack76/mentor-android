package com.mentor.application.views.vendor.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.mentor.application.databinding.LayoutProfessionCategoryBinding
import com.mentor.application.repository.models.Profession
import com.mentor.application.views.vendor.interfaces.EnterDetailInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject


@FragmentScoped
class ProfessionCategoryAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    var mListener: EnterDetailInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mList = mutableListOf<Profession>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListViewHolder(
            LayoutProfessionCategoryBinding.inflate(
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
        val filteredProfessionList = list.map { profession ->
            profession.copy(
                subProfessions = profession.subProfessions?.filter { it.isChecked }
            )
        }
        mList.addAll(filteredProfessionList)
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ListViewHolder).bindListView(position)
    }


    private inner class ListViewHolder(val binding: LayoutProfessionCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {

            binding.tvName.text = mList[absoluteAdapterPosition].profession

            val adapter = ProfessionSubCategoryAdapter(
                mContext!!,
                mList[absoluteAdapterPosition].subProfessions!!,
                false
            )
            val layoutManager = FlexboxLayoutManager(mContext)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.FLEX_START
            layoutManager.alignItems = AlignItems.CENTER
            binding.recyclerView.setLayoutManager(layoutManager)
            binding.recyclerView.adapter = adapter

        }
    }

}