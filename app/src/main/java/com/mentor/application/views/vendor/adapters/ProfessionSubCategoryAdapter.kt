package com.mentor.application.views.vendor.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.R
import com.mentor.application.databinding.LayoutProfessionSubCategoryBinding
import com.mentor.application.repository.models.SubProfession
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject


@FragmentScoped
class ProfessionSubCategoryAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    val mList: List<SubProfession>,
    val isEdit: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListViewHolder(
            LayoutProfessionSubCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ListViewHolder).bindListView(position)
    }


    private inner class ListViewHolder(val binding: LayoutProfessionSubCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {

            binding.btnName.text = mList[absoluteAdapterPosition].subProfession

            if (mList[absoluteAdapterPosition].isChecked) {
                binding.btnName.backgroundTintList = ContextCompat.getColorStateList(
                    mContext!!,
                    R.color.colorPrimary
                )
                binding.btnName.setTextColor(ContextCompat.getColor(mContext, R.color.colorWhite))
            } else {
                binding.btnName.backgroundTintList = ContextCompat.getColorStateList(
                    mContext!!,
                    R.color.colorWhite
                )
                binding.btnName.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.colorPrimaryText
                    )
                )
            }

            if (isEdit) {
                binding.btnName.setOnClickListener {
                    mList[absoluteAdapterPosition].isChecked =
                        !mList[absoluteAdapterPosition].isChecked
                    notifyDataSetChanged()
                }
            }


        }
    }

}