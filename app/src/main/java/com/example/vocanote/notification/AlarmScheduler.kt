package com.example.vocanote.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.vocanote.data.ReminderTime
import java.util.Calendar

object AlarmScheduler {
    private const val REQUEST_CODE_BASE = 1000

    fun scheduleAll(context: Context, times: List<ReminderTime>) {
        times.forEach { schedule(context, it) }
    }

    fun cancelAll(context: Context, times: List<ReminderTime>) {
        times.forEach { cancel(context, it) }
    }

    fun schedule(context: Context, time: ReminderTime) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerMillis(time), pendingIntent(context, time))
    }

    fun cancel(context: Context, time: ReminderTime) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, time))
    }

    private fun pendingIntent(context: Context, time: ReminderTime): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_HOUR, time.hour)
            putExtra(ReminderReceiver.EXTRA_MINUTE, time.minute)
        }
        val requestCode = REQUEST_CODE_BASE + time.hour * 60 + time.minute
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerMillis(time: ReminderTime): Long {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (trigger.timeInMillis <= now.timeInMillis) {
            trigger.add(Calendar.DAY_OF_YEAR, 1)
        }
        return trigger.timeInMillis
    }
}
