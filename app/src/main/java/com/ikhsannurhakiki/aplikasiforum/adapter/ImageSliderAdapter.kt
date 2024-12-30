package com.ikhsannurhakiki.aplikasiforum.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.ImageFileList
import com.ikhsannurhakiki.aplikasiforum.databinding.ItemImageSlideBinding

class ImageSliderAdapter(private val items: List<ImageFileList>) :RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>()  {
    inner class ImageViewHolder(itemView: ItemImageSlideBinding) : RecyclerView.ViewHolder(itemView.root) {
        private val binding = itemView
        fun bind(data: ImageFileList) {
            with(binding){
                Glide.with(itemView)
                    .load(data.file)
                    .fitCenter()
                    .apply(RequestOptions().centerCrop())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(ivSlider)
            }
            Log.d("adap", data.file.toString())
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(ItemImageSlideBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
       holder.bind(items[position])
    }
}