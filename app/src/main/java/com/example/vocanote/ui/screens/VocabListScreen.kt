package com.example.vocanote.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vocanote.data.VocabEntry
import com.example.vocanote.ui.ListDisplayMode
import com.example.vocanote.ui.VocabViewModel
import com.example.vocanote.ui.text.highlightWordsInSentence

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabListScreen(
    viewModel: VocabViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val entries by viewModel.allEntries.collectAsState()

    LaunchedEffect(Unit) { viewModel.resetListDisplayMode() }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("단어장") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                        }
                    }
                )
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).padding(bottom = 8.dp)
                ) {
                    val halfWidth = maxWidth / 2
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = viewModel.listDisplayMode == ListDisplayMode.CARD,
                            onClick = {
                                if (viewModel.listDisplayMode != ListDisplayMode.CARD) viewModel.toggleListDisplayMode()
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            modifier = Modifier.width(halfWidth)
                        ) { Text("카드형") }
                        SegmentedButton(
                            selected = viewModel.listDisplayMode == ListDisplayMode.DETAIL,
                            onClick = {
                                if (viewModel.listDisplayMode != ListDisplayMode.DETAIL) viewModel.toggleListDisplayMode()
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            modifier = Modifier.width(halfWidth)
                        ) { Text("목록형") }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("아직 저장된 단어가 없어요")
            }
        } else {
            val groups = remember(entries) { entries.groupBy { it.groupId }.values.toList() }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when (viewModel.listDisplayMode) {
                    ListDisplayMode.CARD -> items(entries, key = { it.id }) { entry ->
                        FlipCard(entry)
                    }
                    ListDisplayMode.DETAIL -> items(groups, key = { it.first().groupId }) { group ->
                        SentenceGroupCard(
                            entries = group,
                            onEdit = { viewModel.startEdit(group); onEdit() },
                            onDelete = { viewModel.deleteGroup(group.first().groupId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlipCard(entry: VocabEntry) {
    var flipped by remember(entry.id) { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (flipped) 180f else 0f, label = "flip")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { flipped = !flipped }
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            if (rotation <= 90f) {
                Text(
                    entry.word,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().graphicsLayer { rotationY = 180f }
                ) {
                    entry.meaning?.let {
                        Text(it, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Start)
                    }
                    Text(entry.sentence, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Start)
                }
            }
        }
    }
}

@Composable
private fun SentenceGroupCard(entries: List<VocabEntry>, onEdit: () -> Unit, onDelete: () -> Unit) {
    val groupId = entries.first().groupId
    var expanded by remember(groupId) { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { expanded = !expanded }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            val halfWidth = maxWidth / 2
            val highlightColor = MaterialTheme.colorScheme.primary
            val sentence = entries.first().sentence
            val annotatedSentence = highlightWordsInSentence(sentence, entries.map { it.word }, highlightColor)
            Column(modifier = Modifier.padding(end = 100.dp)) {
                Text(
                    annotatedSentence,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        entries.first().sentenceTranslation?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                        }
                        entries.forEach { entry ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text(
                                    entry.word,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(halfWidth)
                                )
                                entry.meaning?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.width(halfWidth)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Row(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "수정")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제")
                }
            }
        }
    }
}
