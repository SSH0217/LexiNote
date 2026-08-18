package com.example.vocanote.data

import android.content.Context

data class ReminderTime(val hour: Int, val minute: Int) : Comparable<ReminderTime> {
    override fun compareTo(other: ReminderTime): Int =
        compareValuesBy(this, other, { it.hour }, { it.minute })

    override fun toString(): String = "%02d:%02d".format(hour, minute)

    companion object {
        fun parse(value: String): ReminderTime? {
            val parts = value.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: return null
            return ReminderTime(hour, minute)
        }
    }
}

object NotificationPreferences {
    private const val PREFS_NAME = "settings"
    private const val KEY_ENABLED = "notifications_enabled"
    private const val KEY_TIMES = "reminder_times"
    private const val KEY_RANDOM_COUNT = "reminder_random_count"
    private const val DEFAULT_RANDOM_COUNT = 20

    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun loadTimes(context: Context): List<ReminderTime> =
        prefs(context).getStringSet(KEY_TIMES, emptySet())
            .orEmpty()
            .mapNotNull { ReminderTime.parse(it) }
            .sorted()

    fun saveTimes(context: Context, times: List<ReminderTime>) {
        prefs(context).edit().putStringSet(KEY_TIMES, times.map { it.toString() }.toSet()).apply()
    }

    fun loadRandomCount(context: Context): Int =
        prefs(context).getInt(KEY_RANDOM_COUNT, DEFAULT_RANDOM_COUNT)

    fun saveRandomCount(context: Context, count: Int) {
        prefs(context).edit().putInt(KEY_RANDOM_COUNT, count).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
