package com.example.vocanote.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocanote.data.TranslationService
import com.example.vocanote.data.VocabDao
import com.example.vocanote.data.VocabEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed class LookupState {
    object Idle : LookupState()
    object Loading : LookupState()
    data class Success(val text: String) : LookupState()
    object Failure : LookupState()
}

enum class ListDisplayMode { CARD, DETAIL }

class VocabViewModel(private val dao: VocabDao) : ViewModel() {

    var currentSentence by mutableStateOf("")
        private set

    var selectedWords by mutableStateOf<Set<String>>(emptySet())
        private set

    var phraseBuilder by mutableStateOf<Set<String>>(emptySet())
        private set

    var editingGroupId by mutableStateOf<String?>(null)
        private set

    var pendingSharedSentence by mutableStateOf<String?>(null)
        private set

    fun requestSharedSentence(text: String) {
        pendingSharedSentence = text
    }

    fun consumePendingSharedSentence() {
        pendingSharedSentence = null
    }

    val wordMeanings = mutableStateMapOf<String, LookupState>()
    val wordMeaningTexts = mutableStateMapOf<String, String>()

    var sentenceTranslation by mutableStateOf<LookupState>(LookupState.Idle)
        private set

    var sentenceTranslationText by mutableStateOf("")
        private set

    val allEntries: StateFlow<List<VocabEntry>> =
        dao.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var listDisplayMode by mutableStateOf(ListDisplayMode.CARD)
        private set

    fun toggleListDisplayMode() {
        listDisplayMode = if (listDisplayMode == ListDisplayMode.CARD) ListDisplayMode.DETAIL else ListDisplayMode.CARD
    }

    fun resetListDisplayMode() {
        listDisplayMode = ListDisplayMode.CARD
    }

    fun setSentence(text: String) {
        currentSentence = text
        clearSelection()
    }

    fun clearSentence() {
        setSentence("")
    }

    fun clearSelection() {
        selectedWords = emptySet()
        phraseBuilder = emptySet()
        editingGroupId = null
        wordMeanings.clear()
        wordMeaningTexts.clear()
        sentenceTranslation = LookupState.Idle
        sentenceTranslationText = ""
    }

    fun startEdit(entries: List<VocabEntry>) {
        if (entries.isEmpty()) return
        editingGroupId = entries.first().groupId
        currentSentence = entries.first().sentence
        phraseBuilder = emptySet()
        wordMeanings.clear()
        wordMeaningTexts.clear()
        entries.forEach { entry ->
            wordMeanings[entry.word] = entry.meaning?.let { LookupState.Success(it) } ?: LookupState.Idle
            wordMeaningTexts[entry.word] = entry.meaning ?: ""
        }
        selectedWords = entries.map { it.word }.toSet()
        val translation = entries.first().sentenceTranslation
        sentenceTranslation = translation?.let { LookupState.Success(it) } ?: LookupState.Idle
        sentenceTranslationText = translation ?: ""
    }

    fun removeWord(word: String) {
        selectedWords = selectedWords - word
        wordMeanings.remove(word)
        wordMeaningTexts.remove(word)
    }

    private fun addWord(word: String) {
        selectedWords = selectedWords + word
        if (wordMeanings[word] != null) return
        wordMeanings[word] = LookupState.Loading
        viewModelScope.launch {
            val result = TranslationService.translate(word)
            val state = result.fold(
                onSuccess = { LookupState.Success(it) },
                onFailure = { LookupState.Failure }
            )
            wordMeanings[word] = state
            if (state is LookupState.Success) {
                wordMeaningTexts[word] = state.text
            }
        }
    }

    fun selectWord(word: String) {
        if (word in selectedWords) {
            selectedWords = selectedWords - word
            return
        }
        addWord(word)
    }

    private fun togglePhraseMembership(word: String): Set<String> =
        if (word in phraseBuilder) phraseBuilder - word else phraseBuilder + word

    fun startPhrase(word: String) {
        phraseBuilder = if (phraseBuilder.isEmpty()) setOf(word) else togglePhraseMembership(word)
    }

    fun togglePhraseWord(word: String) {
        phraseBuilder = togglePhraseMembership(word)
    }

    fun cancelPhrase() {
        phraseBuilder = emptySet()
    }

    fun confirmPhrase(phrase: String) {
        if (phrase.isNotBlank()) addWord(phrase)
        phraseBuilder = emptySet()
    }

    fun setWordMeaningText(word: String, text: String) {
        wordMeaningTexts[word] = text
    }

    fun translateSentence() {
        if (sentenceTranslation !is LookupState.Idle) return
        sentenceTranslation = LookupState.Loading
        viewModelScope.launch {
            val result = TranslationService.translate(currentSentence)
            val state = result.fold(
                onSuccess = { LookupState.Success(it) },
                onFailure = { LookupState.Failure }
            )
            sentenceTranslation = state
            if (state is LookupState.Success) {
                sentenceTranslationText = state.text
            }
        }
    }

    fun updateSentenceTranslationText(text: String) {
        sentenceTranslationText = text
    }

    fun saveEntry() {
        if (selectedWords.isEmpty() || currentSentence.isBlank()) return
        val sentence = currentSentence
        val translation = sentenceTranslationText.ifBlank { null }
        val wordsToSave = selectedWords
        val meaningsSnapshot = wordsToSave.associateWith { wordMeaningTexts[it]?.ifBlank { null } }
        val groupToReplace = editingGroupId
        val groupId = groupToReplace ?: UUID.randomUUID().toString()
        viewModelScope.launch {
            if (groupToReplace != null) {
                dao.deleteByGroupId(groupToReplace)
            }
            wordsToSave.forEach { word ->
                dao.insert(
                    VocabEntry(
                        word = word,
                        sentence = sentence,
                        meaning = meaningsSnapshot[word],
                        sentenceTranslation = translation,
                        groupId = groupId
                    )
                )
            }
            currentSentence = ""
            clearSelection()
        }
    }

    fun deleteEntry(entry: VocabEntry) {
        viewModelScope.launch {
            dao.delete(entry)
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            dao.deleteByGroupId(groupId)
        }
    }
}
