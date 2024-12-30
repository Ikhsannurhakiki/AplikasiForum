package com.ikhsannurhakiki.aplikasiforum.adapter

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.QuestionTagResponse
import com.ikhsannurhakiki.aplikasiforum.databinding.ItemsForumBinding
import com.ikhsannurhakiki.aplikasiforum.ui.home.ForumInterface
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MyQuestionAdapter(
    val forumInterface: ForumInterface,
    private val myQuestionList: List<QuestionTagResponse>
) : RecyclerView.Adapter<MyQuestionAdapter.ViewHolder>() {

    private lateinit var auth: FirebaseAuth

    inner class ViewHolder(private val binding: ItemsForumBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(question: QuestionTagResponse) {
            with(binding) {

                val ref = FirebaseStorage.getInstance().reference.child("img/${question.askedBy}")
                try {
                    val localFile = File.createTempFile("tempImage", ".jpeg")
                    ref.getFile(localFile)
                        .addOnSuccessListener {
                            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                            binding.circleImageView.setImageBitmap(bitmap)
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

                binding.root.setOnClickListener {
                    forumInterface.click(question)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemsForumBinding =
            ItemsForumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemsForumBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(myQuestionList[position])
    }

    override fun getItemCount(): Int = myQuestionList.size

    @SuppressLint("SimpleDateFormat")
    fun calculateTimeAgo(date: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("GMT+7")
        try {
            val time: Long = sdf.parse(date).time
            val now = System.currentTimeMillis()
            val ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
            return "$ago "
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
    }
}