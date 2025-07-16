package com.mentor.application.views.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.databinding.LayoutItemServiceSubCategoryBinding
import com.mentor.application.repository.models.Profession
import com.mentor.application.repository.models.SubProfession
import com.mentor.application.views.customer.interfaces.HomeInterface
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject


@FragmentScoped
class ServiceSubCategoryAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
    var mList: List<SubProfession>,
    var profession: Profession,
    var mListener: HomeInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListViewHolder(
            LayoutItemServiceSubCategoryBinding.inflate(
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


    private inner class ListViewHolder(val binding: LayoutItemServiceSubCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {

//            val s3ImageLoader = S3ImageLoader.getInstance(mContext!!)
//            var imageName =
//                mList[absoluteAdapterPosition].image.split("https://mentorappbuket.s3.us-west-1.amazonaws.com/")
//                    .get(1)
//            s3ImageLoader.loadImage("mentorappbuket", imageName, binding.sdvServiceImage)


            binding.sdvServiceImage.setImageURI(mList[absoluteAdapterPosition].image)
            binding.tvName.text = mList[absoluteAdapterPosition].subProfession

            itemView.setOnClickListener {
                mListener.onCategoryClick(
                    profession, mList[absoluteAdapterPosition]
                )
            }

        }
    }

}