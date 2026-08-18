package com.example.vocanote.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vocanote.data.ThemeMode
import com.example.vocanote.ui.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("테마 설정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Text("다크 모드", style = MaterialTheme.typography.titleMedium)

            ThemeOptionRow("시스템 기본값", ThemeMode.SYSTEM, settingsViewModel)
            ThemeOptionRow("라이트", ThemeMode.LIGHT, settingsViewModel)
            ThemeOptionRow("다크", ThemeMode.DARK, settingsViewModel)
        }
    }
}

@Composable
private fun ThemeOptionRow(label: String, mode: ThemeMode, settingsViewModel: SettingsViewModel) {
    val selected = settingsViewModel.themeMode == mode
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { settingsViewModel.updateThemeMode(mode) }
            .padding(vertical = 4.dp)
    ) {
        RadioButton(selected = selected, onClick = { settingsViewModel.updateThemeMode(mode) })
        Text(label)
    }
}
