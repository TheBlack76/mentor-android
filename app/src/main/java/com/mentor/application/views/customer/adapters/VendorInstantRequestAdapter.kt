package com.mentor.application.views.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.databinding.VendorInstantRequestItemBinding
import com.mentor.application.repository.models.BookingOffer
import com.mentor.application.utils.GeneralFunctions
import com.mentor.application.views.customer.interfaces.BookingOffersInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject


@FragmentScoped
class VendorInstantRequestAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    var mListener: BookingOffersInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mList = mutableListOf<BookingOffer>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListViewHolder(
            VendorInstantRequestItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun updateListData(list: List<BookingOffer>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    fun updateData(data: BookingOffer) {
        mList.add(data)
        notifyItemInserted(mList.size)
    }

    fun onRemove(professionalId:String):List<BookingOffer>{
        mList.removeIf { it.professionalId == professionalId }
        notifyDataSetChanged()
        return mList
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ListViewHolder).bindListView(position)
    }

    private inner class ListViewHolder(val binding: VendorInstantRequestItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {
            binding.tvPrice.text = "$${mList[position].offeredPrice}"
            binding.tvDuration.text = "Duration: ${mList[position].duration}min"
            binding.tvName.text = mList[absoluteAdapterPosition].fullName
            binding.tvRatting.text = mList[absoluteAdapterPosition].rating
            binding.sdvImage.setImageURI(GeneralFunctions.getUserImage(mList[absoluteAdapterPosition].image))

            binding.btnReject.setOnClickListener {
                mListener.onReject(mList[absoluteAdapterPosition])
            }

            binding.btnAccept.setOnClickListener {
                mListener.onAccept(mList[absoluteAdapterPosition])
            }

        }
    }

}