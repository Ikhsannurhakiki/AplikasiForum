package com.ikhsannurhakiki.aplikasiforum.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Report
import com.ikhsannurhakiki.aplikasiforum.databinding.ItemsReportedListBinding
import com.ikhsannurhakiki.aplikasiforum.ui.home.ReportListInterface


class ReportedListAdapter(
    private val reportedInterface: ReportListInterface,
    private val reportedList: List<Report>
) : RecyclerView.Adapter<ReportedListAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemsReportedListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "StringFormatMatches")
        fun bind(result: Report) {
            binding.tvSubject.text = result.probSubject
            Log.d("aaaaaaa", result.probSubject)
            binding.tvDetail.text = result.note
            binding.tvStatus.text = result.status
            when(result.status){
                "Diproses" ->{
                    binding.tvStatus.setBackgroundResource(R.color.yellow)
                }
                "Diterima" ->{
                    binding.tvStatus.setBackgroundResource(R.color.green_500)
                }
                "Ditolak" ->{
                    var txt1 = result.note.substringBefore("//Refuse//")
                    binding.tvStatus.setBackgroundResource(R.color.red)
                    binding.tvDetail.text = txt1
                }
            }


            binding.cardView.setOnClickListener {
                reportedInterface.onClick(result)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemsReportedListBinding =
            ItemsReportedListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemsReportedListBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reportedList[position])
    }

    override fun getItemCount(): Int = reportedList.size
}