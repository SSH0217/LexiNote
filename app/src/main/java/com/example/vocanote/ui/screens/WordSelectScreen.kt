package com.example.vocanote.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vocanote.ui.LookupState
import com.example.vocanote.ui.VocabViewModel
import kotlinx.coroutines.launch

private fun String.toStoredWord(): String = trim { !it.isLetterOrDigit() }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WordSelectScreen(
    viewModel: VocabViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    LaunchedEffect(Unit) { viewModel.translateSentence() }

    val allEntries by viewModel.allEntries.collectAsState()
    val alreadySavedWords = allEntries
        .filter { it.groupId != viewModel.editingGroupId }
        .map { it.word }
        .toSet()

    val tokens = viewModel.currentSentence.split(Regex("\\s+")).filter { it.isNotBlank() }
    val orderedPhrase = tokens.map { it.toStoredWord() }
        .filter { it.isNotEmpty() && it in viewModel.phraseBuilder }
        .distinct()
        .joinToString(" ")

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OriginalTextCard(viewModel.currentSentence)

            TranslationCard(
                state = viewModel.sentenceTranslation,
                text = viewModel.sentenceTranslationText,
                onTextChange = viewModel::updateSentenceTranslationText
            )

            Text(
                "단어를 탭하세요 (꾹 누르면 여러 단어를 묶어 구/숙어로 저장할 수 있어요)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            )
            FlowRow {
                tokens.forEach { token ->
                    val storedWord = token.toStoredWord()
                    val isSelected = storedWord.isNotEmpty() && storedWord in viewModel.selectedWords
                    val isPhraseMember = storedWord.isNotEmpty() && storedWord in viewModel.phraseBuilder
                    val backgroundColor = when {
                        isPhraseMember -> MaterialTheme.colorScheme.tertiaryContainer
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Text(
                        text = token,
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(backgroundColor)
                            .combinedClickable(
                                enabled = storedWord.isNotEmpty(),
                                onClick = {
                                    if (viewModel.phraseBuilder.isNotEmpty()) {
                                        viewModel.togglePhraseWord(storedWord)
                                    } else {
                                        viewModel.selectWord(storedWord)
                                    }
                                },
                                onLongClick = { viewModel.startPhrase(storedWord) }
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        fontWeight = if (isSelected || isPhraseMember) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            if (viewModel.phraseBuilder.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text("구/숙어 미리보기", style = MaterialTheme.typography.labelSmall)
                    Text(
                        orderedPhrase,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        OutlinedButton(onClick = viewModel::cancelPhrase) {
                            Text("취소")
                        }
                        Button(
                            onClick = { viewModel.confirmPhrase(orderedPhrase) },
                            enabled = orderedPhrase.isNotBlank()
                        ) {
                            Text("추가")
                        }
                    }
                }
            }

            if (viewModel.selectedWords.isNotEmpty()) {
                Text(
                    "선택한 단어",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
                Column {
                    viewModel.selectedWords.sorted().forEach { word ->
                        DefinitionRow(
                            word = word,
                            state = viewModel.wordMeanings[word],
                            text = viewModel.wordMeaningTexts[word] ?: "",
                            onTextChange = { viewModel.setWordMeaningText(word, it) },
                            onRemove = { viewModel.removeWord(word) },
                            alreadySaved = word in alreadySavedWords
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 24.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (viewModel.editingGroupId != null) {
                            viewModel.clearSentence()
                        } else {
                            viewModel.clearSelection()
                        }
                        onCancel()
                    }
                ) {
                    Text("취소")
                }
                Button(
                    onClick = {
                        viewModel.saveEntry()
                        onSaved()
                    },
                    enabled = viewModel.selectedWords.isNotEmpty()
                ) {
                    Text(
                        if (viewModel.editingGroupId != null) "수정 저장 (${viewModel.selectedWords.size})"
                        else "저장 (${viewModel.selectedWords.size})"
                    )
                }
            }
        }
    }
}

@Composable
private fun OriginalTextCard(sentence: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("원문", style = MaterialTheme.typography.labelMedium)
            Text(sentence, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun TranslationCard(state: LookupState, text: String, onTextChange: (String) -> Unit) {
    if (state is LookupState.Idle) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("번역", style = MaterialTheme.typography.labelMedium)
            if (state is LookupState.Loading) {
                Row {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Text(" 번역 중...", modifier = Modifier.padding(start = 8.dp))
                }
            } else {
                val bringIntoViewRequester = remember { BringIntoViewRequester() }
                val scope = rememberCoroutineScope()
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusEvent { if (it.isFocused) scope.launch { bringIntoViewRequester.bringIntoView() } },
                    placeholder = { Text(if (state is LookupState.Failure) "번역 실패 - 직접 입력해보세요" else "번역") }
                )
            }
        }
    }
}

@Composable
private fun DefinitionRow(
    word: String,
    state: LookupState?,
    text: String,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit,
    alreadySaved: Boolean
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        if (alreadySaved) {
            Text(
                "이미 저장됨",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        Text(
            word,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (state is LookupState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                val bringIntoViewRequester = remember { BringIntoViewRequester() }
                val scope = rememberCoroutineScope()
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f)
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusEvent { if (it.isFocused) scope.launch { bringIntoViewRequester.bringIntoView() } },
                    singleLine = true,
                    placeholder = { Text(if (state is LookupState.Failure) "뜻을 찾을 수 없어요 - 직접 입력" else "의미") }
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "제거")
            }
        }
    }
}
