package com.example.obdcontrol.setup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.obdcontrol.R

object NotificationSetup {

    fun makeChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            with(context) {
                val name = "status"
                val description = "information about current situation"
                val channel = NotificationChannel(getString(R.string.notification_channel_id), name, NotificationManager.IMPORTANCE_LOW)
                channel.description = description
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.createNotificationChannel(channel)
            }
        }
    }
}