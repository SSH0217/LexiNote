package com.example.vocanote.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val STUDY_REMINDER = "study_reminder"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            STUDY_REMINDER, "학습 알림", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "설정한 시간에 단어 테스트를 알려줘요" }
        manager.createNotificationChannel(channel)
    }
}
