package com.ikhsannurhakiki.aplikasiforum.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import com.google.gson.annotations.SerializedName
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.TagResponse
import com.ikhsannurhakiki.aplikasiforum.databinding.ItemsTagBinding
import com.ikhsannurhakiki.aplikasiforum.ui.home.ForumInterface


class TagAdapter(
    private val tagList: List<TagResponse>,
    val forumInterface: ForumInterface,
    val subjectId: Int,
    val lecturerId: String?
) : RecyclerView.Adapter<TagAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemsTagBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(tag: TagResponse) {
            binding.txtTag.text = "#${tag.tagName}"
            Log.d("adap", tag.tagName)

            binding.txtTag.setOnClickListener{
                forumInterface.onTagClicked(tag.tagName, subjectId, lecturerId)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemsTagBinding =
            ItemsTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemsTagBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tagList[position])


    }

    override fun getItemCount(): Int = tagList.size
}