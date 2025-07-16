package com.mentor.application.views.vendor.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.R
import com.mentor.application.databinding.LayoutBookingRequestIssuesBinding
import com.mentor.application.repository.models.Questions
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject


@FragmentScoped
class BookingRequestIssuesAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mList = mutableListOf<Questions>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListViewHolder(
            LayoutBookingRequestIssuesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun updateData(list: List<Questions>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()

    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ListViewHolder).bindListView(position)
    }


    private inner class ListViewHolder(val binding: LayoutBookingRequestIssuesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {

            binding.tvName.text = mList[absoluteAdapterPosition].question
            binding.tvTagline.text = mList[absoluteAdapterPosition].answer

            if (position == 0) {
                binding.tvTagline.visibility = View.VISIBLE
                binding.tvName.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_upword_arrow,
                    0
                );
            }

            binding.tvName.setOnClickListener {
                if (binding.tvTagline.isVisible) {
                    binding.tvTagline.visibility = View.GONE
                    binding.tvName.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_down_arrow,
                        0
                    );

                } else {
                    binding.tvTagline.visibility = View.VISIBLE
                    binding.tvName.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_upword_arrow,
                        0
                    );

                }
            }

        }
    }

}