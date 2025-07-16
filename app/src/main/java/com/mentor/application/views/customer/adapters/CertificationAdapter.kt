package com.mentor.application.views.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.databinding.LayoutItemCertificationBinding
import com.mentor.application.views.vendor.interfaces.VendorProfileInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import java.io.File
import javax.inject.Inject


@FragmentScoped
class CertificationAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    var mListener: VendorProfileInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mList = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListViewHolder(
            LayoutItemCertificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun updateData(list: List<String>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ListViewHolder).bindListView(position)

    }


    private inner class ListViewHolder(val binding: LayoutItemCertificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {

            binding.tvName.text= File(mList[absoluteAdapterPosition]).name

            binding.sdvDocument.setOnClickListener {
                mListener.onCertificateClick(mList[absoluteAdapterPosition])
            }



        }
    }

}