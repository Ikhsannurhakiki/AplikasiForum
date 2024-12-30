package com.ikhsannurhakiki.aplikasiforum.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Subject
import com.ikhsannurhakiki.aplikasiforum.databinding.ItemsSubject2Binding
import com.ikhsannurhakiki.aplikasiforum.ui.home.SubjectManagementInterface

class Subject2Adapter(
    private val subjectInterface: SubjectManagementInterface,
    private val subjectList: List<Subject>,
    private var accessRight: String
) : RecyclerView.Adapter<Subject2Adapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemsSubject2Binding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(subject: Subject) {
            binding.tvSubject.text = subject.subject + " (" + subject.sks + " SKS)"
            binding.tvSubject.setOnClickListener{
                subjectInterface.onClick(subject.id, subject.subject, accessRight)
            }

            binding.tvSubject.setOnLongClickListener {
                subjectInterface.onLongClick(subject.id, subject.subject, subject.sks)
                true
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemsSubject2Binding =
            ItemsSubject2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemsSubject2Binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(subjectList[position])
    }

    override fun getItemCount(): Int = subjectList.size
}