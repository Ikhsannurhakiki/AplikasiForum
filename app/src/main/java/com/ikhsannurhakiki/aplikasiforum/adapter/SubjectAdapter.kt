package com.ikhsannurhakiki.aplikasiforum.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Subject
import com.ikhsannurhakiki.aplikasiforum.databinding.ItemsSubjectBinding
import com.ikhsannurhakiki.aplikasiforum.ui.home.SubjectInterface

class SubjectAdapter(
    private val subjectInterface: SubjectInterface,
    private val subjectList: List<Subject>,
    private var accessRight: String
) : RecyclerView.Adapter<SubjectAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemsSubjectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(subject: Subject) {
            binding.tvSubject.text = subject.subject

            binding.cardView.setOnClickListener{
                subjectInterface.onClick(subject.id, subject.subject, accessRight)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemsSubjectBinding =
            ItemsSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemsSubjectBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(subjectList[position])
    }

    override fun getItemCount(): Int = subjectList.size
}