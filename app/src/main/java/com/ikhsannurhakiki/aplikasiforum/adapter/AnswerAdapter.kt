package com.ikhsannurhakiki.aplikasiforum.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.ikhsannurhakiki.aplikasiforum.BuildConfig
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Answer
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.ImageFileListUri
import com.ikhsannurhakiki.aplikasiforum.databinding.ItemsAnswerBinding
import com.ikhsannurhakiki.aplikasiforum.ui.home.AnswerInterface
import com.ikhsannurhakiki.aplikasiforum.ui.home.ForumInterface
import com.ikhsannurhakiki.aplikasiforum.ui.home.ImageInterface
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AnswerAdapter(
    private val answerList: List<Answer>,
    val forumInterface: ForumInterface,
    val answerInterface: AnswerInterface,
    val imageInterface: ImageInterface,
    val isPunished : String?,
    val pointUser: Int,
    private val context: Context
) : RecyclerView.Adapter<AnswerAdapter.ViewHolder>() {

    private lateinit var _binding3: ItemsAnswerBinding
    private val binding3 get() = _binding3

    inner class ViewHolder(val binding: ItemsAnswerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(answer: Answer) {
            with(binding) {
                if(isPunished == "Tipe 1"){
                    btnScoreUp.isEnabled = false
                    btnScoreDown.isEnabled = false
                    btnDetails.isEnabled = false
                }
                val ref = FirebaseStorage.getInstance().reference.child("img/${answer.answerById}")
                try {
                    val localFile = File.createTempFile("tempImage", ".jpeg")
                    ref.getFile(localFile)
                        .addOnSuccessListener {
                            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                            Glide.with(context.applicationContext)
                                .load(bitmap)
                                .centerCrop()
                                .into(binding.circleImageView)
                        }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                tvAnswer.text = answer.answer
                tvTimeAgo.text = "${calculateTimeAgo(answer.date)}"
                tvAnswerBy.text = answer.name
                tvScore.text = answer.totalPoint.toString()
                lottieVerified.isVisible = answer.verified == 1

                btnDetails.setOnClickListener {
                    forumInterface.answerThreeDotsClicked(answer)
                }
                var dbScore = answer.voted.score
                var dbTotalPoint = answer.totalPoint
                var s = "insert"
                val point = pointUser
                var score = pointUser
                if (dbScore > 0) {        //VotedUp
                    btnScoreUp.setOnClickListener {
                        Log.d("aaaaaaaaaaaaaaaaaaaaa", tvScore.text.toString())
                        var txtScore = Integer.parseInt(tvScore.text.toString())
                        when (txtScore) {
                            dbTotalPoint -> {
                                s = "delete"
                                txtScore -= point
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = false
                            }
                            dbTotalPoint - point -> {
                                s = "insert"
                                txtScore += point
                                score = pointUser
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = true
                                btnScoreDown.isSelected = false
                            }
                            dbTotalPoint - (2 * point) -> {
                                s = "delete"
                                txtScore += point
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = false
                            }
                        }
                        answerInterface.voteHandler(answer.answerId, s, score)
                    }
                    btnScoreDown.setOnClickListener {
                        var txtScore = Integer.parseInt(tvScore.text.toString())
                        when (txtScore) {
                            dbTotalPoint -> {
                                s = "delete"
                                txtScore -= point
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = false
                            }
                            dbTotalPoint - point -> {
                                s = "insert"
                                txtScore -= point
                                score = pointUser*-1
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = true
                            }
                            dbTotalPoint - (2 * point) -> {
                                s = "delete"
                                txtScore += point
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = false
                            }
                        }
                        answerInterface.voteHandler(answer.answerId, s, score)
                    }
                }else if (dbScore < 0) {        //VotedDown
                    btnScoreUp.setOnClickListener {
                        var txtScore = Integer.parseInt(tvScore.text.toString())
                        when (txtScore) {
                            dbTotalPoint -> {
                                s = "delete"
                                txtScore += point
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = false
                            }
                            dbTotalPoint + point -> {
                                s = "insert"
                                txtScore += point
                                score = pointUser
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = true
                                btnScoreDown.isSelected = false
                            }
                            dbTotalPoint + (2 * point) -> {
                                s = "delete"
                                txtScore -= point
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = false
                            }
                        }
                        answerInterface.voteHandler(answer.answerId, s, score)
                    }
                    btnScoreDown.setOnClickListener {
                        var txtScore = Integer.parseInt(tvScore.text.toString())
                        when (txtScore) {
                            dbTotalPoint -> {
                                s = "delete"
                                txtScore += point
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = false
                            }
                            dbTotalPoint + point -> {
                                s = "insert"
                                txtScore -= point
                                score = pointUser*-1
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = true
                            }
                            dbTotalPoint + (2 * point) -> {
                                s = "delete"
                                txtScore -= point
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = false
                            }
                        }
                        answerInterface.voteHandler(answer.answerId, s, score)
                    }
                }else{
                    btnScoreUp.setOnClickListener {
                        var txtScore = Integer.parseInt(tvScore.text.toString())
                        when (txtScore) {
                            dbTotalPoint -> {
                                s = "insert"
                                txtScore += point
                                score = pointUser
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = true
                                btnScoreDown.isSelected = false
                            }
                            dbTotalPoint + point -> {
                                s = "delete"
                                txtScore -= point
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = false
                            }
                            dbTotalPoint - point -> {
                                s = "delete"
                                txtScore += point
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = false
                            }
                        }
                        answerInterface.voteHandler(answer.answerId, s, score)
                    }
                    btnScoreDown.setOnClickListener {
                        var txtScore = Integer.parseInt(tvScore.text.toString())
                        when (txtScore) {
                            dbTotalPoint -> {
                                s = "insert"
                                txtScore -= point
                                score = pointUser*-1
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = true
                            }
                            dbTotalPoint - point -> {
                                s = "delete"
                                txtScore += point
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = false
                            }
                            dbTotalPoint + point -> {
                                s = "delete"
                                txtScore -= point
                                tvScore.text = txtScore.toString()
                                btnScoreUp.isSelected = false
                                btnScoreDown.isSelected = false
                            }
                        }
                        answerInterface.voteHandler(answer.answerId, s, score)
                    }
                }

//                when (answer.editedDate) {
//                    answer.editedDate -> tvTimeEdit.text = "${itemView.context.getString(R.string.edited)} -"
//                    else -> "${itemView.context.getString(R.string.edited)} ${calculateTimeAgo(answer.editedDate)}"
//                }

            }
        }
    }

    private fun setButtonClicked(isClicked: String, holder: ViewHolder) {
        Log.d("aaaaaaaaaaaaaa", isClicked)
        when (isClicked) {
            "UP" -> {
                holder.binding.btnScoreUp.isSelected = true
                holder.binding.btnScoreDown.isSelected = false
            }
            "DOWN" -> {
                holder.binding.btnScoreUp.isSelected = false
                holder.binding.btnScoreDown.isSelected = true
            }
            else -> {
                holder.binding.btnScoreUp.isSelected = false
                holder.binding.btnScoreDown.isSelected = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemsAnswerBinding =
            ItemsAnswerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemsAnswerBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val answer = answerList[position]
        answer.let { holder.bind(it) }
        val images = answer.images
        val listImages = ArrayList<ImageFileListUri>()
        var dots: ArrayList<TextView> = ArrayList()
        holder.binding.dotsIndicator.removeAllViews()

        for (element in images) {
            val uri = Uri.parse(BuildConfig.BASE_URL + "/uploads/answer/" + element.images)
            listImages.add(
                ImageFileListUri(uri)
            )
        }
        if (listImages.size != 0) {
            holder.binding.pageNumberText.text = "1 / ${listImages.size}"
        }
        setIndicator(listImages, dots, holder)
        holder.binding.viewPager.adapter = ImageSliderAdapterUri(listImages, imageInterface)
        holder.binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                holder.binding.pageNumberText.text = "${position + 1} / ${listImages.size}"
                if (listImages.isNotEmpty()) {
                    for (i in 0 until listImages.size) {
                        if (i == position)
                            dots[i].setTextColor(
                                ContextCompat.getColor(
                                    holder.itemView.context,
                                    R.color.green_200
                                )
                            )
                        else
                            dots[i].setTextColor(
                                ContextCompat.getColor(
                                    holder.itemView.context,
                                    R.color.lightGrey
                                )
                            )
                    }
                }
                super.onPageSelected(position)
            }
        })

        val voted = answer.voted
        Log.d("aaaaaaaaaaaaaa", voted.score.toString())
        if (voted.score > 0) {
            setButtonClicked("UP", holder)
        } else if (voted.score < 0) {
            setButtonClicked("DOWN", holder)
        } else {
            setButtonClicked("NONE", holder)
        }

    }

    private fun setIndicator(
        listImages: ArrayList<ImageFileListUri>,
        dots: ArrayList<TextView>,
        holder: ViewHolder
    ) {
        if (listImages.isNotEmpty()) {
            holder.binding.viewPager.isVisible = true
            holder.binding.dotsIndicator.isVisible = true
            for (i in 0 until listImages.size) {
                dots.add(TextView(holder.itemView.context))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    dots[i].text = Html.fromHtml("&#9679", Html.FROM_HTML_MODE_LEGACY).toString()
                } else {
                    dots[i].text = Html.fromHtml("&#9679")
                }
                dots[i].textSize = 15f
                holder.binding.dotsIndicator.addView(dots[i])
            }
        } else {
            holder.binding.viewPager.isVisible = false
            holder.binding.dotsIndicator.isVisible = false
        }
    }

    override fun getItemCount(): Int = answerList.size

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