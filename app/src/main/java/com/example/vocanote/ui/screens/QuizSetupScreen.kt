package com.example.vocanote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vocanote.ui.QuizViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSetupScreen(
    quizViewModel: QuizViewModel,
    onBack: () -> Unit,
    onStart: () -> Unit
) {
    var totalCount by remember { mutableIntStateOf(-1) }
    var recentCount by remember { mutableFloatStateOf(10f) }
    var questionCount by remember { mutableFloatStateOf(10f) }
    var randomCount by remember { mutableFloatStateOf(20f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val count = quizViewModel.availableCount()
        totalCount = count
        if (recentCount > count) recentCount = count.coerceAtLeast(1).toFloat()
    }

    val recentMax = totalCount.toFloat().coerceAtLeast(1f)
    if (recentCount > recentMax) recentCount = recentMax
    if (questionCount > recentCount) questionCount = recentCount

    val randomMax = if (totalCount >= 30) 30f else totalCount.toFloat().coerceAtLeast(1f)
    val randomMin = if (totalCount >= 30) 10f else 1f
    if (randomCount > randomMax) randomCount = randomMax
    if (randomCount < randomMin) randomCount = randomMin

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("테스트 준비하기") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (totalCount == -1) {
            return@Scaffold
        }
        if (totalCount in 0..2) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "저장된 단어가 부족해요",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "단어를 3개 이상 저장한 후 테스트를 만들어보세요!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            return@Scaffold
        }
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp)
        ) {
            val topHeight = maxHeight * 0.5f
            val dividerHeight = 64.dp
            val bottomHeight = maxHeight - topHeight - dividerHeight
            val dividerLineWidth = (maxWidth - 48.dp) / 2

            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().height(topHeight)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("범위를 선택하세요", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Text(
                            "최근 ${recentCount.toInt()}개 중에서 (전체 ${totalCount}개)",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Slider(
                            value = recentCount,
                            onValueChange = { recentCount = it },
                            valueRange = 1f..recentMax,
                            steps = (recentMax.toInt() - 2).coerceAtLeast(0)
                        )

                        Text(
                            "${questionCount.toInt()}문제 풀기",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Slider(
                            value = questionCount,
                            onValueChange = { questionCount = it },
                            valueRange = 1f..recentCount.coerceAtLeast(1f),
                            steps = (recentCount.toInt() - 2).coerceAtLeast(0)
                        )

                        Button(
                            onClick = {
                                scope.launch {
                                    if (quizViewModel.startRecent(recentCount.toInt(), questionCount.toInt())) onStart()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            Text("테스트 시작하기")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().height(dividerHeight),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.width(dividerLineWidth).height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Text(
                        "  or  ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier.width(dividerLineWidth).height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }

                Box(modifier = Modifier.fillMaxWidth().height(bottomHeight)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("랜덤 문제", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Text(
                            "${randomCount.toInt()}문제",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Slider(
                            value = randomCount,
                            onValueChange = { randomCount = it },
                            valueRange = randomMin..randomMax,
                            steps = (randomMax.toInt() - randomMin.toInt() - 1).coerceAtLeast(0)
                        )

                        Button(
                            onClick = {
                                scope.launch {
                                    if (quizViewModel.startRandom(randomCount.toInt())) onStart()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            Text("랜덤 테스트 시작하기")
                        }

                        quizViewModel.setupMessage?.let {
                            Text(
                                it,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
