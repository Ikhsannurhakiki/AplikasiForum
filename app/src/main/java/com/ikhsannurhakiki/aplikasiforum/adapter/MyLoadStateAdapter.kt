package com.ikhsannurhakiki.aplikasiforum.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ikhsannurhakiki.aplikasiforum.databinding.LoadStateViewBinding


class MyLoadStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<MyLoadStateAdapter.LoadStateViewHolder>() {

    inner class LoadStateViewHolder(private val binding: LoadStateViewBinding)
        :RecyclerView.ViewHolder(binding.root){
            init {
                binding.loadStateRetry.setOnClickListener{
                    retry.invoke()
                }
            }
        fun bind(loadState: LoadState){
            with(binding){
                loadStateProgress.isVisible = loadState is LoadState.Loading
                loadStateRetry.isVisible = loadState is LoadState.Error
                loadStateErrorMessage.isVisible= loadState is LoadState.NotLoading
            }
        }
        }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
      holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val binding = LoadStateViewBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return LoadStateViewHolder(binding)
    }
}