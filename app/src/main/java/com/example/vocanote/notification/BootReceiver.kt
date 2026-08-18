package com.example.vocanote.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.vocanote.data.NotificationPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!NotificationPreferences.isEnabled(context)) return
        AlarmScheduler.scheduleAll(context, NotificationPreferences.loadTimes(context))
    }
}
