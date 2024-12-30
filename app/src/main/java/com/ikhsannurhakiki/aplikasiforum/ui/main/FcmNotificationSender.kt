package com.ikhsannurhakiki.aplikasiforum.ui.main

import android.app.Activity
import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ikhsannurhakiki.aplikasiforum.BuildConfig.FCM_POST_URL
import com.ikhsannurhakiki.aplikasiforum.BuildConfig.FCM_SERVER_KEY
import com.ikhsannurhakiki.aplikasiforum.R
import com.ikhsannurhakiki.aplikasiforum.data.source.remote.response.QuestionTagResponse
import org.json.JSONException
import org.json.JSONObject


public class FcmNotificationSender {
    private lateinit var userFcmToken: String
    private lateinit var title: String
    private lateinit var body: String
    private lateinit var context: Context
    private lateinit var question:QuestionTagResponse
    private lateinit var activity: Activity
    private lateinit var requestQueue: RequestQueue
    private val postUrl: String = FCM_POST_URL
    private val fcmServerKey: String = FCM_SERVER_KEY
    private var subjectId:Int = 0
    private var lecturerId:String = ""
    private var askedBy:String = ""
    private var questionId:Int = 0
    private var subjectName:String = ""


    fun fcmNotificationSender(
        userFcmToken: String,
        subjectId:Int,
        lecturerId: String,
        askedBy:String,
        questionId:Int,
        subjectName:String,
        title: String,
        body: String,
        context: Context,
        activity: Activity
    ) {
        this.userFcmToken = userFcmToken
        this.subjectId = subjectId
        this.lecturerId = lecturerId
        this.askedBy = askedBy
        this.questionId = questionId
        this.subjectName = subjectName
        this.title = title
        this.body = body
        this.context = context
        this.activity = activity
    }

    fun sendNotification() {
        requestQueue = Volley.newRequestQueue(activity)
        val mainObj: JSONObject = JSONObject()
        try {
            mainObj.put("to", userFcmToken)
            val notifObject: JSONObject = JSONObject()
            notifObject.put("title", title)
            notifObject.put("body", body)
            notifObject.put("icon", R.drawable.ic_baseline_message_24)
            notifObject.put("SUBJECTID", subjectId)
            notifObject.put("LECTURERID", lecturerId)
            notifObject.put("ASKEDBYID", askedBy)
            notifObject.put("QUESTIONID", questionId)
            notifObject.put("SUBJECTNAME", subjectName)
            mainObj.put("notification", notifObject)


            val jsonObjectRequest: JsonObjectRequest =
                object : JsonObjectRequest(Request.Method.POST, postUrl, mainObj,
                    Response.Listener<JSONObject?> { },
                    Response.ErrorListener { }) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["Authorization"] = "key=$fcmServerKey"
                        params["Content-Type"] = "application/json"
                        return params
                    }
                }
            requestQueue.add(jsonObjectRequest)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


}