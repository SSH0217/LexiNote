package com.example.vocanote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vocanote.ui.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizPlayScreen(
    quizViewModel: QuizViewModel,
    onQuit: () -> Unit,
    onFinished: () -> Unit
) {
    val question = quizViewModel.currentQuestion

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("문제 ${quizViewModel.currentIndex + 1} / ${quizViewModel.questions.size}") },
                navigationIcon = {
                    IconButton(onClick = { quizViewModel.reset(); onQuit() }) {
                        Icon(Icons.Default.Close, contentDescription = "그만두기")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (question == null) {
            return@Scaffold
        }
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Text(question.entry.word, style = MaterialTheme.typography.headlineSmall)
            Text(
                question.entry.sentence,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            question.choices.forEachIndexed { index, choice ->
                val selected = quizViewModel.selectedIndex
                val isSpecial = selected != null && (index == question.correctIndex || index == selected)
                val background = when {
                    selected == null -> MaterialTheme.colorScheme.surfaceVariant
                    index == question.correctIndex -> Color(0xFFA5D6A7)
                    index == selected -> Color(0xFFEF9A9A)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                Text(
                    text = choice,
                    color = if (isSpecial) Color(0xFF1B1B1B) else Color.Unspecified,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(background)
                        .clickable(enabled = selected == null) { quizViewModel.selectAnswer(index) }
                        .padding(16.dp)
                )
            }

            if (quizViewModel.selectedIndex != null) {
                Button(
                    onClick = {
                        if (quizViewModel.isLastQuestion) {
                            onFinished()
                        } else {
                            quizViewModel.nextQuestion()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                ) {
                    Text(if (quizViewModel.isLastQuestion) "결과 보기" else "다음")
                }
            }
        }
    }
}
