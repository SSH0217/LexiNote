package com.example.vocanote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vocanote.data.NotificationPreferences
import com.example.vocanote.data.ThemeMode
import com.example.vocanote.data.VocaNoteDatabase
import com.example.vocanote.notification.NotificationChannels
import com.example.vocanote.notification.ReminderReceiver
import com.example.vocanote.ui.AppNav
import com.example.vocanote.ui.NotificationSettingsViewModel
import com.example.vocanote.ui.NotificationSettingsViewModelFactory
import com.example.vocanote.ui.QuizViewModel
import com.example.vocanote.ui.QuizViewModelFactory
import com.example.vocanote.ui.SettingsViewModel
import com.example.vocanote.ui.SettingsViewModelFactory
import com.example.vocanote.ui.VocabViewModel
import com.example.vocanote.ui.VocabViewModelFactory
import com.example.vocanote.ui.theme.VocaNoteTheme

class MainActivity : ComponentActivity() {
    private lateinit var vocabViewModel: VocabViewModel
    private lateinit var quizViewModel: QuizViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationChannels.ensureCreated(applicationContext)

        val dao = VocaNoteDatabase.getInstance(applicationContext).vocabDao()
        val settingsFactory = SettingsViewModelFactory(applicationContext)
        val notificationFactory = NotificationSettingsViewModelFactory(applicationContext)

        vocabViewModel = ViewModelProvider(this, VocabViewModelFactory(dao))[VocabViewModel::class.java]
        quizViewModel = ViewModelProvider(this, QuizViewModelFactory(dao))[QuizViewModel::class.java]
        handleIncomingIntent(intent)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)
            val notificationViewModel: NotificationSettingsViewModel = viewModel(factory = notificationFactory)

            val darkTheme = when (settingsViewModel.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val view = LocalView.current
            SideEffect {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }

            VocaNoteTheme(darkTheme = darkTheme, dynamicColor = false) {
                AppNav(vocabViewModel, quizViewModel, settingsViewModel, notificationViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_PROCESS_TEXT -> {
                val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()?.trim()
                if (!text.isNullOrBlank()) vocabViewModel.requestSharedSentence(text)
            }
            ReminderReceiver.ACTION_START_RANDOM_QUIZ -> {
                val count = NotificationPreferences.loadRandomCount(applicationContext)
                quizViewModel.requestAutoStart(count)
            }
        }
    }
}
