package com.example.vocanote.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vocanote.ui.screens.NotificationSettingsScreen
import com.example.vocanote.ui.screens.QuizPlayScreen
import com.example.vocanote.ui.screens.QuizResultScreen
import com.example.vocanote.ui.screens.QuizSetupScreen
import com.example.vocanote.ui.screens.SentenceInputScreen
import com.example.vocanote.ui.screens.SettingsScreen
import com.example.vocanote.ui.screens.ThemeSettingsScreen
import com.example.vocanote.ui.screens.VocabListScreen
import com.example.vocanote.ui.screens.WordSelectScreen

object Routes {
    const val INPUT = "input"
    const val WORD_SELECT = "word_select"
    const val LIST = "list"
    const val QUIZ_SETUP = "quiz_setup"
    const val QUIZ_PLAY = "quiz_play"
    const val QUIZ_RESULT = "quiz_result"
    const val SETTINGS = "settings"
    const val THEME_SETTINGS = "theme_settings"
    const val NOTIFICATION_SETTINGS = "notification_settings"
}

@Composable
fun AppNav(
    viewModel: VocabViewModel,
    quizViewModel: QuizViewModel,
    settingsViewModel: SettingsViewModel,
    notificationViewModel: NotificationSettingsViewModel
) {
    val navController = rememberNavController()

    LaunchedEffect(viewModel.pendingSharedSentence) {
        val text = viewModel.pendingSharedSentence
        if (text != null) {
            viewModel.setSentence(text)
            navController.popBackStack(Routes.INPUT, inclusive = false)
            viewModel.consumePendingSharedSentence()
        }
    }

    LaunchedEffect(quizViewModel.pendingAutoStartCount) {
        val count = quizViewModel.pendingAutoStartCount
        if (count != null) {
            val started = quizViewModel.startRandom(count)
            quizViewModel.consumeAutoStartRequest()
            if (started) {
                navController.navigate(Routes.QUIZ_PLAY) {
                    popUpTo(Routes.INPUT) { inclusive = false }
                }
            } else {
                navController.popBackStack(Routes.INPUT, inclusive = false)
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.INPUT) {
        composable(Routes.INPUT) {
            SentenceInputScreen(
                viewModel = viewModel,
                onNext = { navController.navigate(Routes.WORD_SELECT) },
                onOpenList = { navController.navigate(Routes.LIST) },
                onOpenQuiz = { navController.navigate(Routes.QUIZ_SETUP) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.WORD_SELECT) {
            WordSelectScreen(
                viewModel = viewModel,
                onSaved = { navController.popBackStack(Routes.INPUT, inclusive = false) },
                onCancel = { navController.popBackStack(Routes.INPUT, inclusive = false) }
            )
        }
        composable(Routes.LIST) {
            VocabListScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.WORD_SELECT) }
            )
        }
        composable(Routes.QUIZ_SETUP) {
            QuizSetupScreen(
                quizViewModel = quizViewModel,
                onBack = { navController.popBackStack() },
                onStart = { navController.navigate(Routes.QUIZ_PLAY) }
            )
        }
        composable(Routes.QUIZ_PLAY) {
            QuizPlayScreen(
                quizViewModel = quizViewModel,
                onQuit = { navController.popBackStack(Routes.INPUT, inclusive = false) },
                onFinished = {
                    navController.navigate(Routes.QUIZ_RESULT) {
                        popUpTo(Routes.QUIZ_SETUP) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.QUIZ_RESULT) {
            QuizResultScreen(
                quizViewModel = quizViewModel,
                onDone = { navController.popBackStack(Routes.INPUT, inclusive = false) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenTheme = { navController.navigate(Routes.THEME_SETTINGS) },
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATION_SETTINGS) }
            )
        }
        composable(Routes.THEME_SETTINGS) {
            ThemeSettingsScreen(
                settingsViewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.NOTIFICATION_SETTINGS) {
            NotificationSettingsScreen(
                notificationViewModel = notificationViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
