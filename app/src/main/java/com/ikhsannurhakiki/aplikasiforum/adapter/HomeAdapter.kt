package com.ikhsannurhakiki.aplikasiforum.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.QuestionTagResponse
import com.ikhsannurhakiki.aplikasiforum.databinding.ItemsForumBinding
import com.ikhsannurhakiki.aplikasiforum.ui.home.ForumInterface
import com.ikhsannurhakiki.aplikasiforum.viewmodel.HomeViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class HomeAdapter(
    val context: Context,
    val lifecycleOwner: LifecycleOwner,
    val forumInterface: ForumInterface,
    val subjectId: Int,
    val lecturerId: String?,
    val viewModel: HomeViewModel
) :
    PagingDataAdapter<QuestionTagResponse, HomeAdapter.ViewHolder>(DIFF_CALLBACK) {

    private lateinit var auth: FirebaseAuth
    private val viewPool = RecyclerView.RecycledViewPool()

    inner class ViewHolder(private val binding: ItemsForumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var rv: RecyclerView? = null

        @SuppressLint("SetTextI18n")
        fun bind(question: QuestionTagResponse) {
            with(binding) {
                rv = binding.rvTag
                    var ref =
                        FirebaseStorage.getInstance().reference.child("img/${question.askedBy}")
                    val requestOptions = RequestOptions()
                        .signature(ObjectKey(System.currentTimeMillis())) // Add this line to ensure unique image loading for each item
                        .placeholder(R.drawable.img)
                        .error(R.drawable.img)
                    try {
                        val localFile = File.createTempFile(question.askedBy, ".jpeg")
                        ref.getFile(localFile)
                            .addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                Glide.with(itemView)
                                    .load(bitmap)
                                    .apply(requestOptions)
                                    .into(binding.circleImageView)
                            }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                tvTitle.text = question.title
                tvQuestion.text = question.question
                tvViews.text = question.views.toString()
                auth = FirebaseAuth.getInstance()

                if ((auth.currentUser?.uid ?: String()) == question.askedBy) {
                    tvAskedBy.text = "${question.name} (${
                        itemView.context.getString(
                            R.string.you
                        )
                    })"
                } else {
                    tvAskedBy.text = question.name
                }

                val timeAgo = forumInterface.calculateTimeAgo(question.date)
                tvTimeAgo.text = timeAgo

                tvScore.text = question.totalPoint.toString()
                tvAnswer.text = question.totalAnswer.toString()
                tvStatus.text = question.status
                if (question.status == "belum selesai") {
                    tvStatus.setBackgroundResource(R.color.yellow)
                } else {
                    tvStatus.setBackgroundResource(R.color.green_500)
                }

//                forumInterface.getTagsByQuestion(question.id)
                binding.root.setOnClickListener {
                    forumInterface.click(question)
                }

                binding.circleImageView.setOnClickListener {
                    forumInterface.onImageClicked(question.askedBy)
                }
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemsForumBinding =
            ItemsForumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemsForumBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val question = getItem(position)
        question?.let { holder.bind(it) }
        holder.rv?.apply {
            layoutManager =
                LinearLayoutManager(holder.rv?.context, RecyclerView.HORIZONTAL, false)
            adapter =
                question?.tag?.let { TagAdapter(it, forumInterface, subjectId, lecturerId) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<QuestionTagResponse>() {
            override fun areItemsTheSame(
                oldItem: QuestionTagResponse,
                newItem: QuestionTagResponse
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: QuestionTagResponse,
                newItem: QuestionTagResponse
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    private fun bitmapToFile(bitmap: Bitmap, askedBy: String): File {
        val file: File = File(context.cacheDir, "${askedBy}.jpg")
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

}