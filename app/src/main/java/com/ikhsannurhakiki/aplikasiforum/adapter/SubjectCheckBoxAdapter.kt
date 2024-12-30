package com.ikhsannurhakiki.aplikasiforum.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Subject
import com.ikhsannurhakiki.aplikasiforum.databinding.ItemsCheckboxBinding
import com.ikhsannurhakiki.aplikasiforum.ui.auth.RegisterLecturerInterface

class SubjectCheckBoxAdapter(
    private val lecturerRegisterInterface: RegisterLecturerInterface,
    private val subjectList: List<Subject>,
    private val subjectSuppList: List<Subject>?
) : RecyclerView.Adapter<SubjectCheckBoxAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemsCheckboxBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(subject: Subject) {
            with(binding) {
                checkBox.text = subject.subject
//                val subjectSuppList: List<Subject> = listOf(Subject(1, "Algoritma dan Pemrograman", "2"), Subject (3, "Kalkulus", "2"))
                val subjectToBeCheckedSet: Set<String> =
                    subjectSuppList?.map { it.subject }?.toSet() ?: emptySet()

                if (subjectSuppList?.isNotEmpty() == true) {
                    checkBox.isChecked = subjectToBeCheckedSet.contains(subject.subject)
                }
                if (checkBox.isChecked) {
                    lecturerRegisterInterface.checkBoxChecked(subject, true)
                }
                checkBox.setOnClickListener {
                    if (checkBox.isChecked) {
                        lecturerRegisterInterface.checkBoxChecked(subject, true)
                    } else {
                        lecturerRegisterInterface.checkBoxChecked(subject, false)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemsCheckboxBinding =
            ItemsCheckboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemsCheckboxBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(subjectList[position])
    }

    override fun getItemCount(): Int = subjectList.size
}