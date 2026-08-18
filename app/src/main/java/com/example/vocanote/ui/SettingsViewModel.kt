package com.example.vocanote.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.vocanote.data.ThemeMode
import com.example.vocanote.data.ThemePreferences

class SettingsViewModel(private val appContext: Context) : ViewModel() {

    var themeMode by mutableStateOf(ThemePreferences.load(appContext))
        private set

    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
        ThemePreferences.save(appContext, mode)
    }
}
