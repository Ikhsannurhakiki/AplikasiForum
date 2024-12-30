package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ikhsannurhakiki.aplikasiforum.databinding.ActivityPopUpImageBinding

class PopUpImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPopUpImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopUpImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uriString = intent.getStringExtra("URI")
        val uri = Uri.parse(uriString)

        Glide.with(this)
            .load(uri)
            .fitCenter()
            .apply(RequestOptions().centerCrop())
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.iv)
    }
}