package com.ikhsannurhakiki.aplikasiforum.ui.home

import android.text.format.DateUtils
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.Answer
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.QuestionTagResponse
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

interface ForumInterface {

    fun click(question: QuestionTagResponse)

    fun onImageClicked(userId: String)

    fun onTagClicked(tag: String, subjectId:Int, lecturerId: String?)

    fun answerThreeDotsClicked(answer: Answer)

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

//    fun votingUpClicked()
//
//    fun votingDownClicked()
}