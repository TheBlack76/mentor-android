package com.mentor.application.views.customer.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mentor.application.databinding.LayoutHelpUsQuestionsItemBinding
import com.mentor.application.repository.models.Questions
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import java.io.File
import javax.inject.Inject


@FragmentScoped
class HelpUsQuestionAdapter @Inject constructor(
    @ActivityContext val mContext: Context?,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mList = mutableListOf<String>()
    private var mAnswerList = mutableListOf<Questions>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ListViewHolder(
            LayoutHelpUsQuestionsItemBinding.inflate(
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
        list.map {
            mAnswerList.add(Questions(question = it))
        }
        notifyDataSetChanged()
    }

    fun getAnswerList():List<Questions>{
        return mAnswerList
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ListViewHolder).bindListView(position)

    }


    private inner class ListViewHolder(val binding: LayoutHelpUsQuestionsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListView(
            position: Int
        ) {

            binding.tvName.text = File(mList[absoluteAdapterPosition]).name

            binding.etAnswer.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    mAnswerList[position].answer=s.toString()
                }
            })

        }
    }

}