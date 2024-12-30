package com.ikhsannurhakiki.aplikasiforum.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.ImageFileListUri
import com.ikhsannurhakiki.aplikasiforum.databinding.ItemImageSlideBinding
import com.ikhsannurhakiki.aplikasiforum.ui.home.ImageInterface

class ImageSliderAdapterUri(private val items: List<ImageFileListUri>, private val imageInterface: ImageInterface) :RecyclerView.Adapter<ImageSliderAdapterUri.ImageViewHolder>()  {
    inner class ImageViewHolder(itemView: ItemImageSlideBinding) : RecyclerView.ViewHolder(itemView.root) {
        private val binding = itemView
        fun bind(data: ImageFileListUri) {
            with(binding){
                Glide.with(itemView)
                    .load(data.uri)
                    .fitCenter()
                    .apply(RequestOptions().centerCrop())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(ivSlider)
            ivSlider.setOnClickListener {
                imageInterface.onImageClicked(data.uri)
            }
            }
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