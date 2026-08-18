package com.example.vocanote.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.vocanote.MainActivity
import com.example.vocanote.R
import com.example.vocanote.data.NotificationPreferences
import com.example.vocanote.data.ReminderTime

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val hour = intent.getIntExtra(EXTRA_HOUR, -1)
        val minute = intent.getIntExtra(EXTRA_MINUTE, -1)
        if (hour < 0 || minute < 0) return

        val time = ReminderTime(hour, minute)
        if (NotificationPreferences.isEnabled(context) && time in NotificationPreferences.loadTimes(context)) {
            AlarmScheduler.schedule(context, time)
        }

        showNotification(context)
    }

    private fun showNotification(context: Context) {
        NotificationChannels.ensureCreated(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_START_RANDOM_QUIZ
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.STUDY_REMINDER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("공부할 시간입니다")
            .setContentText("테스트를 해보세요!")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
        const val ACTION_START_RANDOM_QUIZ = "com.example.vocanote.action.START_RANDOM_QUIZ"
        const val NOTIFICATION_ID = 1
    }
}
