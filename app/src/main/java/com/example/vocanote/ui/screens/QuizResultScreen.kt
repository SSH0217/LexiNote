package com.example.vocanote.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vocanote.ui.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    quizViewModel: QuizViewModel,
    onDone: () -> Unit
) {
    val questions = quizViewModel.questions
    val wrongIndices = questions.indices.filter {
        quizViewModel.userAnswers.getOrNull(it) != questions[it].correctIndex
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("결과") }) },
        bottomBar = {
            Button(
                onClick = { quizViewModel.reset(); onDone() },
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp)
            ) {
                Text("메인으로")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Text(
                "${questions.size}문제 중 ${quizViewModel.correctCount}개 정답",
                style = MaterialTheme.typography.headlineSmall
            )

            if (wrongIndices.isNotEmpty()) {
                Text(
                    "틀린 문제",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(wrongIndices) { index ->
                        val question = questions[index]
                        val myAnswer = quizViewModel.userAnswers.getOrNull(index)
                            ?.let { question.choices.getOrNull(it) } ?: "미응답"
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(question.entry.word, style = MaterialTheme.typography.titleMedium)
                                Text("정답: ${question.choices[question.correctIndex]}")
                                Text("내 답: $myAnswer")
                            }
                        }
                    }
                }
            }
        }
    }
}
