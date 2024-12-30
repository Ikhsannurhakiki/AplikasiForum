package com.ikhsannurhakiki.aplikasiforum.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Report
import com.ikhsannurhakiki.aplikasiforum.databinding.ItemsReportedListBinding


class TagERAdapter(
    private val tagList: List<Report>,
) : RecyclerView.Adapter<TagERAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemsReportedListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(tag: Report) {
            binding.tvSubject.text = "#${tag.reportedName}"
            Log.d("size", tagList.size.toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemsReportedListBinding =
            ItemsReportedListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemsReportedListBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tagList[position])
    }

    override fun getItemCount(): Int = tagList.size
}