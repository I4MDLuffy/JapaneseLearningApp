package app.kotori.japanese.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kotori.japanese.AppContainer
import app.kotori.japanese.data.db.KnownItemEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewItem(
    val type: String,
    val itemId: String,
    val title: String,    // Japanese form (kanji / word)
    val reading: String,  // Hiragana reading
    val meaning: String,  // English meaning
    val extra: String = "", // Romaji or additional info
)

sealed class ReviewState {
    data object Loading : ReviewState()
    data object Empty : ReviewState()
    data class Reviewing(
        val items: List<ReviewItem>,
        val currentIndex: Int = 0,
        val isRevealed: Boolean = false,
        val correctCount: Int = 0,
        val incorrectCount: Int = 0,
    ) : ReviewState() {
        val current get() = items[currentIndex]
        val progress get() = currentIndex.toFloat() / items.size.toFloat()
        val isLast get() = currentIndex == items.size - 1
    }
    data class Finished(val correct: Int, val total: Int) : ReviewState()
}

class ReviewViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow<ReviewState>(ReviewState.Loading)
    val state: StateFlow<ReviewState> = _state.asStateFlow()

    init {
        loadDueItems()
    }

    private fun loadDueItems() {
        viewModelScope.launch {
            _state.value = ReviewState.Loading
            val dueItems = container.knownRepository.getDueItems(limit = 30)
            if (dueItems.isEmpty()) {
                _state.value = ReviewState.Empty
                return@launch
            }
            val reviewItems = dueItems.mapNotNull { buildReviewItem(it) }.shuffled()
            _state.value = if (reviewItems.isEmpty()) ReviewState.Empty
            else ReviewState.Reviewing(items = reviewItems)
        }
    }

    fun reveal() {
        val s = _state.value as? ReviewState.Reviewing ?: return
        _state.value = s.copy(isRevealed = true)
    }

    fun recordResult(correct: Boolean) {
        val s = _state.value as? ReviewState.Reviewing ?: return
        viewModelScope.launch {
            container.knownRepository.recordReview(s.current.type, s.current.itemId, correct)
        }
        val newCorrect = s.correctCount + if (correct) 1 else 0
        val newIncorrect = s.incorrectCount + if (correct) 0 else 1
        if (s.isLast) {
            _state.value = ReviewState.Finished(
                correct = newCorrect,
                total = s.items.size,
            )
        } else {
            _state.value = s.copy(
                currentIndex = s.currentIndex + 1,
                isRevealed = false,
                correctCount = newCorrect,
                incorrectCount = newIncorrect,
            )
        }
    }

    fun restart() { loadDueItems() }

    private suspend fun buildReviewItem(known: KnownItemEntity): ReviewItem? {
        return try {
            when (known.type) {
                "kanji" -> container.kanjiRepository.getKanjiById(known.itemId)?.let {
                    ReviewItem(known.type, known.itemId, it.kanji, it.hiragana, it.meaning)
                }
                "verb" -> container.verbRepository.getVerbById(known.itemId)?.let {
                    ReviewItem(known.type, known.itemId,
                        it.kanji.ifBlank { it.dictionaryForm }, it.dictionaryForm, it.meaning, it.romaji)
                }
                "adjective" -> container.adjectiveRepository.getAdjectiveById(known.itemId)?.let {
                    ReviewItem(known.type, known.itemId,
                        it.kanji.ifBlank { it.hiragana }, it.hiragana, it.meaning, it.romaji)
                }
                "noun" -> container.nounRepository.getNounById(known.itemId)?.let {
                    ReviewItem(known.type, known.itemId,
                        it.kanji.ifBlank { it.hiragana }, it.hiragana, it.meaning, it.romaji)
                }
                "grammar" -> container.grammarRepository.getGrammarById(known.itemId)?.let {
                    ReviewItem(known.type, known.itemId, it.title, "Lesson ${it.lessonNumber}", it.content.take(120))
                }
                "phrase" -> container.phraseRepository.getPhraseById(known.itemId)?.let {
                    ReviewItem(known.type, known.itemId, it.phrase, it.reading, it.meaning, it.romaji)
                }
                else -> null
            }
        } catch (_: Exception) { null }
    }
}
