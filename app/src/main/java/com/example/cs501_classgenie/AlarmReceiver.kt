package com.example.cs501_classgenie

import android.annotation.SuppressLint
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("receive","${intent.getStringExtra("textTitle")}")
        val notification: Notification =
            NotificationCompat.Builder(context, context.getString(R.string.channel_id))
                .setSmallIcon(R.drawable.ic_arrow)
                .setContentTitle(intent.getStringExtra("textTitle"))
                .setContentText(intent.getStringExtra("textContent"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        // notificationId is a unique int for each notification that you must define.
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(114514, notification)

    }

    companion object {

        val requestCode = 1919810

    }
}