package com.ikhsannurhakiki.aplikasiforum.ui.main

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_SOUND
import androidx.core.app.NotificationCompat.DEFAULT_VIBRATE
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ikhsannurhakiki.aplikasiforum.BuildConfig
import com.ikhsannurhakiki.aplikasiforum.ui.home.DetailQuestionActivity


class FirebaseNotificationService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("MyFirebase", "onNewToken: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
//        createNotificationChannel()
        sendNotification(this, remoteMessage)
    }


    private fun sendNotification(context: Context, remoteMessage: RemoteMessage) {

        val extraSubjectId = remoteMessage.data["SUBJECTID"]
        val extraLecturerId = remoteMessage.data["LECTURERID"]
        val extraAskedBy = remoteMessage.data["ASKEDBYID"]
        val extraQuestionId = remoteMessage.data["QUESTIONID"]
        val extraSubjectName = remoteMessage.data["SUBJECTNAME"]

        val contentIntent = Intent(applicationContext, DetailQuestionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK}
        contentIntent.putExtra("SUBJECTID", extraSubjectId)
        contentIntent.putExtra("LECTURERID", extraLecturerId)
        contentIntent.putExtra("ASKEDBYID", extraAskedBy)
        contentIntent.putExtra("QUESTIONID", extraQuestionId)
        contentIntent.putExtra("SUBJECTNAME", extraSubjectName)
        val notifyPendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(com.ikhsannurhakiki.aplikasiforum.R.drawable.ic_baseline_message_24)
            .setContentTitle(remoteMessage.notification!!.title)
            .setContentText(remoteMessage.notification!!.body)
            .setAutoCancel(true)
            .setContentIntent(notifyPendingIntent)
            .setDefaults(DEFAULT_SOUND or DEFAULT_VIBRATE)
            .setPriority(Notification.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O &&
            notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            builder.setChannelId(CHANNEL_ID)
            channel.enableVibration(true)
            channel.enableLights(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(channel)
        }
            notificationManager.notify(NOTIFICATION_ID, builder.build())
    }



    private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())

    companion object {
        private const val NOTIFICATION_ID = 2
        private const val CHANNEL_NAME = "ForumApp channel"
        const val CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"
    }

}