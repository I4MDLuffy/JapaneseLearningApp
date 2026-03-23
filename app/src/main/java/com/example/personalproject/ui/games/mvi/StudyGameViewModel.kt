package com.example.personalproject.ui.games.mvi

import androidx.lifecycle.viewModelScope
import com.example.personalproject.data.model.VocabularyWord
import com.example.personalproject.data.repository.SavedRepository
import com.example.personalproject.data.repository.VocabularyRepository
import com.example.personalproject.mvi.BaseViewModel
import com.example.personalproject.ui.games.HiraganaUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MATCH_BATCH_SIZE = 12
private const val MULTI_OPTIONS = 4
private const val QUIZ_TICKS = 50   // 50 × 100 ms = 5 s
private const val SPEED_TICKS = 30  // 30 × 100 ms = 3 s
private const val TICK_MS = 100L
private const val SWIPE_TILE_COUNT = 8

class StudyGameViewModel(
    private val gameType: GameType,
    private val setKey: String,
    private val vocabularyRepository: VocabularyRepository,
    private val savedRepository: SavedRepository,
) : BaseViewModel<StudyGameState, StudyGameAction>(StudyGameState()) {

    private var timerJob: Job? = null
    private var allWords: List<VocabularyWord> = emptyList()
    private var batches: List<List<VocabularyWord>> = emptyList()

    init { loadAndStart() }

    override fun dispatchAction(action: StudyGameAction) {
        when (action) {
            is StudyGameAction.Restart -> { timerJob?.cancel(); loadAndStart() }

            // Flashcard
            is StudyGameAction.FlipCard -> handleFlip()
            is StudyGameAction.GotIt -> handleFlashcardAdvance(gotIt = true)
            is StudyGameAction.ReviewAgain -> handleFlashcardAdvance(gotIt = false)

            // Timed Quiz
            is StudyGameAction.SelectOption -> handleSelectOption(action.option)
            is StudyGameAction.QuizTimerUpdate -> handleQuizTimerUpdate(action.fraction)
            is StudyGameAction.QuizTimerExpired -> handleQuizTimerExpired()

            // Match Pairs
            is StudyGameAction.TapCard -> handleCardTap(action.index)
            is StudyGameAction.SetPairMode -> handleSetPairMode(action.mode)

            // Kana Speed
            is StudyGameAction.TapGridTile -> handleTapGridTile(action.index)
            is StudyGameAction.SpeedTimerUpdate -> handleSpeedTimerUpdate(action.fraction)
            is StudyGameAction.SpeedTimerExpired -> handleSpeedTimerExpired()

            // Kana Swipe
            is StudyGameAction.TapSwipeTile -> handleTapSwipeTile(action.index)
            is StudyGameAction.UndoSwipe -> handleUndoSwipe()
            is StudyGameAction.SubmitSwipe -> handleSubmitSwipe()
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    private fun loadAndStart() {
        viewModelScope.launch {
            updateState { copy(phase = StudyGamePhase.Loading, error = null) }
            try {
                val words = loadWords()
                allWords = words
                batches = words.shuffled().chunked(MATCH_BATCH_SIZE)
                startGame(words.shuffled())
            } catch (e: Exception) {
                updateState { copy(error = e.message ?: "Failed to load") }
            }
        }
    }

    private suspend fun loadWords(): List<VocabularyWord> = when (setKey) {
        "saved_vocabulary" -> {
            val ids = savedRepository.getSavedItemIds("vocabulary")
            ids.mapNotNull { vocabularyRepository.getWordById(it) }
        }
        else -> {
            val parts = setKey.split("_")
            val jlpt = when (parts.getOrNull(0)) {
                "beginner" -> "N5"
                "intermediate" -> "N4"
                "advanced" -> "N3"
                "master" -> "N2"
                else -> "N5"
            }
            val setIndex = parts.getOrNull(2)?.toIntOrNull() ?: 0
            vocabularyRepository.filterByJlpt(jlpt).sortedBy { it.id }.chunked(5).getOrElse(setIndex) { emptyList() }
        }
    }

    private fun startGame(words: List<VocabularyWord>) {
        timerJob?.cancel()
        when (gameType) {
            GameType.FLASHCARDS -> updateState {
                copy(phase = StudyGamePhase.Flashcard(words))
            }
            GameType.TIMED_QUIZ -> {
                updateState {
                    copy(phase = StudyGamePhase.TimedQuiz(
                        entries = words,
                        options = buildOptions(words, 0),
                    ))
                }
                launchQuizTimer()
            }
            GameType.MATCH_PAIRS -> {
                val mode = uiState.value.pairMode
                updateState {
                    copy(phase = StudyGamePhase.MatchPairs(
                        entries = words,
                        cards = buildMatchCards(batches.getOrElse(0) { words.take(MATCH_BATCH_SIZE) }, mode),
                        totalBatches = batches.size,
                    ))
                }
            }
            GameType.KANA_SPEED -> {
                val speedWords = words.filter { it.hiragana.isNotBlank() }.ifEmpty { words }
                val first = speedWords.getOrNull(0)
                updateState {
                    copy(phase = StudyGamePhase.KanaSpeed(
                        entries = speedWords,
                        gridTiles = if (first != null) HiraganaUtils.buildGrid(first.hiragana) else emptyList(),
                    ))
                }
                launchSpeedTimer()
            }
            GameType.KANA_SWIPE -> {
                val swipeWords = words
                    .filter { it.hiragana.isNotBlank() && HiraganaUtils.decompose(it.hiragana).size <= 6 }
                    .ifEmpty { words.filter { it.hiragana.isNotBlank() }.ifEmpty { words } }
                val first = swipeWords.getOrNull(0)
                updateState {
                    copy(phase = StudyGamePhase.KanaSwipe(
                        entries = swipeWords,
                        tiles = if (first != null) HiraganaUtils.buildGrid(first.hiragana, SWIPE_TILE_COUNT) else emptyList(),
                    ))
                }
            }
            // KANJI_DROP and KANJI_BUILDER are handled by their own screen composables
            else -> updateState { copy(phase = StudyGamePhase.Loading) }
        }
    }

    // ── Flashcard ─────────────────────────────────────────────────────────────

    private fun handleFlip() {
        val phase = uiState.value.phase as? StudyGamePhase.Flashcard ?: return
        updateState { copy(phase = phase.copy(isRevealed = true)) }
    }

    private fun handleFlashcardAdvance(gotIt: Boolean) {
        val phase = uiState.value.phase as? StudyGamePhase.Flashcard ?: return
        val next = phase.currentIndex + 1
        val newGotIt = if (gotIt) phase.gotItCount + 1 else phase.gotItCount
        if (next >= phase.entries.size) {
            updateState { copy(phase = StudyGamePhase.Results(newGotIt, phase.entries.size, GameType.FLASHCARDS)) }
        } else {
            updateState { copy(phase = phase.copy(currentIndex = next, isRevealed = false, gotItCount = newGotIt)) }
        }
    }

    // ── Timed Quiz ────────────────────────────────────────────────────────────

    private fun buildOptions(entries: List<VocabularyWord>, index: Int): List<String> {
        val correct = entries[index].english
        val distractors = allWords.map { it.english }
            .filter { it != correct }
            .shuffled()
            .take(MULTI_OPTIONS - 1)
        return (distractors + correct).shuffled()
    }

    private fun handleSelectOption(option: String) {
        val phase = uiState.value.phase as? StudyGamePhase.TimedQuiz ?: return
        if (phase.selectedOption != null) return
        timerJob?.cancel()
        val correct = option == phase.entries[phase.currentIndex].english
        updateState { copy(phase = phase.copy(selectedOption = option, isCorrect = correct, score = if (correct) phase.score + 1 else phase.score)) }
        viewModelScope.launch { delay(900); advanceQuiz() }
    }

    private fun handleQuizTimerUpdate(fraction: Float) {
        val phase = uiState.value.phase as? StudyGamePhase.TimedQuiz ?: return
        if (phase.selectedOption != null) return
        updateState { copy(phase = phase.copy(timeRemaining = fraction)) }
    }

    private fun handleQuizTimerExpired() {
        val phase = uiState.value.phase as? StudyGamePhase.TimedQuiz ?: return
        if (phase.selectedOption != null) return
        updateState { copy(phase = phase.copy(selectedOption = "", isCorrect = false, timeRemaining = 0f)) }
        viewModelScope.launch { delay(900); advanceQuiz() }
    }

    private fun advanceQuiz() {
        val phase = uiState.value.phase as? StudyGamePhase.TimedQuiz ?: return
        val next = phase.currentIndex + 1
        if (next >= phase.entries.size) {
            updateState { copy(phase = StudyGamePhase.Results(phase.score, phase.entries.size, GameType.TIMED_QUIZ)) }
        } else {
            updateState {
                copy(phase = phase.copy(
                    currentIndex = next,
                    options = buildOptions(phase.entries, next),
                    selectedOption = null,
                    isCorrect = null,
                    timeRemaining = 1f,
                ))
            }
            launchQuizTimer()
        }
    }

    private fun launchQuizTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (tick in QUIZ_TICKS downTo 0) {
                delay(TICK_MS)
                val phase = uiState.value.phase as? StudyGamePhase.TimedQuiz ?: return@launch
                if (phase.selectedOption != null) return@launch
                val fraction = tick.toFloat() / QUIZ_TICKS.toFloat()
                if (tick == 0) dispatchAction(StudyGameAction.QuizTimerExpired)
                else dispatchAction(StudyGameAction.QuizTimerUpdate(fraction))
            }
        }
    }

    // ── Match Pairs ───────────────────────────────────────────────────────────

    private fun buildMatchCards(entries: List<VocabularyWord>, mode: PairMode): List<StudyMatchCard> =
        entries.flatMap { w ->
            listOf(
                StudyMatchCard(pairId = w.id, text = w.japanese, isJapanese = true),
                StudyMatchCard(pairId = w.id, text = if (mode == PairMode.ENGLISH) w.english else w.romaji, isJapanese = false),
            )
        }.shuffled()

    private fun handleSetPairMode(mode: PairMode) {
        val phase = uiState.value.phase as? StudyGamePhase.MatchPairs ?: return
        val currentBatchWords = batches.getOrElse(phase.batchIndex) { phase.entries.take(MATCH_BATCH_SIZE) }
        updateState {
            copy(
                pairMode = mode,
                phase = phase.copy(
                    cards = buildMatchCards(currentBatchWords, mode),
                    firstSelectedIndex = null,
                    matchedIds = emptySet(),
                    wrongIndices = emptySet(),
                ),
            )
        }
    }

    private fun handleCardTap(index: Int) {
        val phase = uiState.value.phase as? StudyGamePhase.MatchPairs ?: return
        if (phase.wrongIndices.isNotEmpty()) return
        val card = phase.cards[index]
        if (phase.matchedIds.contains(card.pairId)) return
        val firstIdx = phase.firstSelectedIndex
        when {
            firstIdx == null -> updateState { copy(phase = phase.copy(firstSelectedIndex = index)) }
            firstIdx == index -> updateState { copy(phase = phase.copy(firstSelectedIndex = null)) }
            else -> {
                val firstCard = phase.cards[firstIdx]
                val isMatch = firstCard.pairId == card.pairId && firstCard.isJapanese != card.isJapanese
                if (isMatch) {
                    val newMatched = phase.matchedIds + card.pairId
                    val batchDone = newMatched.size == phase.cards.size / 2
                    updateState { copy(phase = phase.copy(matchedIds = newMatched, firstSelectedIndex = null)) }
                    if (batchDone) {
                        val newScore = phase.totalScore + newMatched.size
                        val nextBatch = phase.batchIndex + 1
                        viewModelScope.launch {
                            delay(500)
                            if (nextBatch >= batches.size) {
                                updateState { copy(phase = StudyGamePhase.Results(newScore, allWords.size, GameType.MATCH_PAIRS)) }
                            } else {
                                val mode = uiState.value.pairMode
                                updateState {
                                    copy(phase = StudyGamePhase.MatchPairs(
                                        entries = phase.entries,
                                        cards = buildMatchCards(batches[nextBatch], mode),
                                        batchIndex = nextBatch,
                                        totalBatches = batches.size,
                                        totalScore = newScore,
                                    ))
                                }
                            }
                        }
                    }
                } else {
                    updateState { copy(phase = phase.copy(firstSelectedIndex = null, wrongIndices = setOf(firstIdx, index))) }
                    viewModelScope.launch {
                        delay(600)
                        val cur = uiState.value.phase as? StudyGamePhase.MatchPairs ?: return@launch
                        updateState { copy(phase = cur.copy(wrongIndices = emptySet())) }
                    }
                }
            }
        }
    }

    // ── Kana Speed ────────────────────────────────────────────────────────────

    private fun handleTapGridTile(index: Int) {
        val phase = uiState.value.phase as? StudyGamePhase.KanaSpeed ?: return
        if (phase.feedback != null) return
        val target = HiraganaUtils.decompose(phase.entries[phase.currentIndex].hiragana)
        val nextExpected = target.getOrNull(phase.tappedIndices.size) ?: return
        if (phase.gridTiles.getOrNull(index) == nextExpected) {
            val newTapped = phase.tappedIndices + index
            if (newTapped.size == target.size) {
                timerJob?.cancel()
                updateState { copy(phase = phase.copy(tappedIndices = newTapped, feedback = StudyFeedback.CORRECT, score = phase.score + 1)) }
                viewModelScope.launch { delay(900); advanceSpeed() }
            } else {
                updateState { copy(phase = phase.copy(tappedIndices = newTapped)) }
            }
        } else {
            timerJob?.cancel()
            updateState { copy(phase = phase.copy(feedback = StudyFeedback.WRONG)) }
            viewModelScope.launch { delay(900); advanceSpeed() }
        }
    }

    private fun handleSpeedTimerUpdate(fraction: Float) {
        val phase = uiState.value.phase as? StudyGamePhase.KanaSpeed ?: return
        if (phase.feedback != null) return
        updateState { copy(phase = phase.copy(timeRemaining = fraction)) }
    }

    private fun handleSpeedTimerExpired() {
        val phase = uiState.value.phase as? StudyGamePhase.KanaSpeed ?: return
        if (phase.feedback != null) return
        updateState { copy(phase = phase.copy(feedback = StudyFeedback.WRONG, timeRemaining = 0f)) }
        viewModelScope.launch { delay(900); advanceSpeed() }
    }

    private fun advanceSpeed() {
        val phase = uiState.value.phase as? StudyGamePhase.KanaSpeed ?: return
        val next = phase.currentIndex + 1
        if (next >= phase.entries.size) {
            updateState { copy(phase = StudyGamePhase.Results(phase.score, phase.entries.size, GameType.KANA_SPEED)) }
        } else {
            val nextWord = phase.entries[next]
            updateState {
                copy(phase = phase.copy(
                    currentIndex = next,
                    gridTiles = HiraganaUtils.buildGrid(nextWord.hiragana),
                    tappedIndices = emptyList(),
                    feedback = null,
                    timeRemaining = 1f,
                ))
            }
            launchSpeedTimer()
        }
    }

    private fun launchSpeedTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (tick in SPEED_TICKS downTo 0) {
                delay(TICK_MS)
                val phase = uiState.value.phase as? StudyGamePhase.KanaSpeed ?: return@launch
                if (phase.feedback != null) return@launch
                val fraction = tick.toFloat() / SPEED_TICKS.toFloat()
                if (tick == 0) dispatchAction(StudyGameAction.SpeedTimerExpired)
                else dispatchAction(StudyGameAction.SpeedTimerUpdate(fraction))
            }
        }
    }

    // ── Kana Swipe ────────────────────────────────────────────────────────────

    private fun handleTapSwipeTile(index: Int) {
        val phase = uiState.value.phase as? StudyGamePhase.KanaSwipe ?: return
        if (phase.feedback != null) return
        if (phase.path.contains(index)) return  // already in path
        updateState { copy(phase = phase.copy(path = phase.path + index)) }
    }

    private fun handleUndoSwipe() {
        val phase = uiState.value.phase as? StudyGamePhase.KanaSwipe ?: return
        if (phase.feedback != null || phase.path.isEmpty()) return
        updateState { copy(phase = phase.copy(path = phase.path.dropLast(1))) }
    }

    private fun handleSubmitSwipe() {
        val phase = uiState.value.phase as? StudyGamePhase.KanaSwipe ?: return
        if (phase.feedback != null || phase.path.isEmpty()) return
        val word = phase.entries.getOrNull(phase.currentIndex) ?: return
        val target = HiraganaUtils.decompose(word.hiragana)
        val answer = phase.path.map { phase.tiles.getOrElse(it) { "" } }
        val correct = answer == target
        val newScore = if (correct) phase.score + 1 else phase.score
        updateState { copy(phase = phase.copy(feedback = if (correct) StudyFeedback.CORRECT else StudyFeedback.WRONG, score = newScore)) }
        viewModelScope.launch { delay(1000); advanceSwipe() }
    }

    private fun advanceSwipe() {
        val phase = uiState.value.phase as? StudyGamePhase.KanaSwipe ?: return
        val next = phase.currentIndex + 1
        if (next >= phase.entries.size) {
            updateState { copy(phase = StudyGamePhase.Results(phase.score, phase.entries.size, GameType.KANA_SWIPE)) }
        } else {
            val nextWord = phase.entries[next]
            updateState {
                copy(phase = phase.copy(
                    currentIndex = next,
                    tiles = HiraganaUtils.buildGrid(nextWord.hiragana, SWIPE_TILE_COUNT),
                    path = emptyList(),
                    feedback = null,
                ))
            }
        }
    }
}
