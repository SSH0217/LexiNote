package com.example.vocanote.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.vocanote.data.NotificationPreferences
import com.example.vocanote.data.ReminderTime
import com.example.vocanote.notification.AlarmScheduler

class NotificationSettingsViewModel(private val appContext: Context) : ViewModel() {

    var notificationsEnabled by mutableStateOf(NotificationPreferences.isEnabled(appContext))
        private set

    var reminderTimes by mutableStateOf(NotificationPreferences.loadTimes(appContext))
        private set

    var randomQuestionCount by mutableStateOf(NotificationPreferences.loadRandomCount(appContext))
        private set

    fun updateNotificationsEnabled(enabled: Boolean) {
        notificationsEnabled = enabled
        NotificationPreferences.setEnabled(appContext, enabled)
        if (enabled) {
            AlarmScheduler.scheduleAll(appContext, reminderTimes)
        } else {
            AlarmScheduler.cancelAll(appContext, reminderTimes)
        }
    }

    fun addReminderTime(time: ReminderTime) {
        if (time in reminderTimes) return
        reminderTimes = (reminderTimes + time).sorted()
        NotificationPreferences.saveTimes(appContext, reminderTimes)
        if (notificationsEnabled) AlarmScheduler.schedule(appContext, time)
    }

    fun removeReminderTime(time: ReminderTime) {
        reminderTimes = reminderTimes - time
        NotificationPreferences.saveTimes(appContext, reminderTimes)
        AlarmScheduler.cancel(appContext, time)
    }

    fun updateRandomQuestionCount(count: Int) {
        randomQuestionCount = count
        NotificationPreferences.saveRandomCount(appContext, count)
    }
}
