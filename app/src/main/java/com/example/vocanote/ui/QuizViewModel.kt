package com.example.vocanote.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.vocanote.data.VocabDao
import com.example.vocanote.data.VocabEntry
import kotlinx.coroutines.flow.first

data class QuizQuestion(
    val entry: VocabEntry,
    val choices: List<String>,
    val correctIndex: Int
)

class QuizViewModel(private val dao: VocabDao) : ViewModel() {

    var questions by mutableStateOf<List<QuizQuestion>>(emptyList())
        private set

    var currentIndex by mutableStateOf(0)
        private set

    var selectedIndex by mutableStateOf<Int?>(null)
        private set

    val userAnswers = mutableStateListOf<Int?>()

    var setupMessage by mutableStateOf<String?>(null)
        private set

    var pendingAutoStartCount by mutableStateOf<Int?>(null)
        private set

    fun requestAutoStart(count: Int) {
        pendingAutoStartCount = count
    }

    fun consumeAutoStartRequest() {
        pendingAutoStartCount = null
    }

    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentIndex)

    val isLastQuestion: Boolean
        get() = currentIndex == questions.lastIndex

    val correctCount: Int
        get() = questions.indices.count { userAnswers.getOrNull(it) == questions[it].correctIndex }

    // 같은 단어가 여러 문장에서 저장돼도 퀴즈에는 한 번만 나오도록, 단어별 가장 최근 항목만 남긴다.
    private suspend fun dedupedPool(): List<VocabEntry> =
        dao.getAll().first().filter { !it.meaning.isNullOrBlank() }.distinctBy { it.word }

    suspend fun availableCount(): Int = dedupedPool().size

    suspend fun startRecent(recentCount: Int, questionCount: Int): Boolean = start { all ->
        val pool = all.take(recentCount.coerceIn(1, all.size))
        pool.shuffled().take(questionCount.coerceIn(1, pool.size))
    }

    suspend fun startRandom(count: Int): Boolean = start { all -> all.shuffled().take(count.coerceIn(1, all.size)) }

    private suspend fun start(pickPool: (List<VocabEntry>) -> List<VocabEntry>): Boolean {
        val all = dedupedPool()
        if (all.isEmpty()) {
            setupMessage = "뜻이 저장된 단어가 없어요"
            return false
        }
        val pool = pickPool(all)
        val allMeanings = all.map { it.meaning!! }.distinct()
        questions = pool.map { entry ->
            val correct = entry.meaning!!
            val choices = (allMeanings.filter { it != correct }.shuffled().take(3) + correct).shuffled()
            QuizQuestion(entry, choices, choices.indexOf(correct))
        }
        currentIndex = 0
        selectedIndex = null
        userAnswers.clear()
        setupMessage = null
        return true
    }

    fun selectAnswer(index: Int) {
        if (selectedIndex != null) return
        selectedIndex = index
        userAnswers.add(index)
    }

    fun nextQuestion() {
        if (currentIndex < questions.lastIndex) {
            currentIndex++
            selectedIndex = null
        }
    }

    fun reset() {
        questions = emptyList()
        currentIndex = 0
        selectedIndex = null
        userAnswers.clear()
        setupMessage = null
    }
}
