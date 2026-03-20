package com.example.personalproject.ui.basiccharacters

import androidx.lifecycle.viewModelScope
import com.example.personalproject.data.kana.KanaEntry
import com.example.personalproject.mvi.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MATCH_BATCH_SIZE = 4
private const val SPEED_TICKS = 30       // 30 × 100 ms = 3 s
private const val SPEED_TICK_MS = 100L
private const val MULTI_CHOICE_OPTIONS = 4

class KanaGameViewModel(
    private val allEntries: List<KanaEntry>,
) : BaseViewModel<KanaGameState, KanaGameAction>(KanaGameState()) {

    private val batches: List<List<KanaEntry>> = allEntries.shuffled().chunked(MATCH_BATCH_SIZE)
    private var timerJob: Job? = null

    override fun dispatchAction(action: KanaGameAction) {
        when (action) {
            is KanaGameAction.SelectMode       -> handleSelectMode(action.mode)
            is KanaGameAction.TapMatchCard     -> handleMatchCardTap(action.index)
            is KanaGameAction.UpdateTypingInput -> handleTypingInput(action.text)
            is KanaGameAction.SubmitTyping     -> handleTypingSubmit()
            is KanaGameAction.FlipFlashcard    -> handleFlipFlashcard()
            is KanaGameAction.AnswerFlashcard  -> handleAnswerFlashcard(action.correct)
            is KanaGameAction.SelectChoice     -> handleSelectChoice(action.choice)
            is KanaGameAction.UpdateSpeedInput -> handleSpeedInput(action.text)
            is KanaGameAction.SubmitSpeed      -> handleSpeedSubmit()
            is KanaGameAction.SpeedTimerUpdate -> handleSpeedTimerUpdate(action.fraction)
            is KanaGameAction.SpeedTimerExpired -> handleSpeedTimerExpired()
            is KanaGameAction.Restart          -> {
                timerJob?.cancel()
                updateState { copy(phase = KanaGamePhase.ModeSelect) }
            }
        }
    }

    // ── Mode selection ────────────────────────────────────────────────────────

    private fun handleSelectMode(mode: GameMode) {
        timerJob?.cancel()
        when (mode) {
            GameMode.MATCHING       -> startMatching()
            GameMode.TYPING         -> startTyping()
            GameMode.FLASHCARD      -> startFlashcard()
            GameMode.MULTIPLE_CHOICE -> startMultipleChoice()
            GameMode.KANA_SPEED     -> startKanaSpeed()
        }
    }

    // ── Matching ──────────────────────────────────────────────────────────────

    private fun startMatching() {
        if (batches.isEmpty()) return
        updateState {
            copy(phase = KanaGamePhase.Matching(
                cards = buildMatchCards(batches[0]),
                batchIndex = 0,
                totalBatches = batches.size,
                totalScore = 0,
            ))
        }
    }

    private fun buildMatchCards(entries: List<KanaEntry>): List<MatchCard> =
        entries.flatMap { e ->
            listOf(
                MatchCard(pairId = e.romaji, text = e.kana, isKana = true),
                MatchCard(pairId = e.romaji, text = e.romaji, isKana = false),
            )
        }.shuffled()

    private fun handleMatchCardTap(index: Int) {
        val phase = uiState.value.phase as? KanaGamePhase.Matching ?: return
        if (phase.wrongIndices.isNotEmpty()) return
        val card = phase.cards[index]
        if (phase.matchedIds.contains(card.pairId)) return
        val firstIdx = phase.firstSelectedIndex
        when {
            firstIdx == null -> updateState { copy(phase = phase.copy(firstSelectedIndex = index)) }
            firstIdx == index -> updateState { copy(phase = phase.copy(firstSelectedIndex = null)) }
            else -> {
                val firstCard = phase.cards[firstIdx]
                val isMatch = firstCard.pairId == card.pairId && firstCard.isKana != card.isKana
                if (isMatch) {
                    val newMatched = phase.matchedIds + card.pairId
                    val batchDone = newMatched.size == phase.cards.size / 2
                    updateState { copy(phase = phase.copy(matchedIds = newMatched, firstSelectedIndex = null)) }
                    if (batchDone) {
                        val newScore = phase.totalScore + phase.cards.size / 2
                        val nextBatch = phase.batchIndex + 1
                        viewModelScope.launch {
                            delay(500)
                            if (nextBatch >= batches.size) {
                                updateState { copy(phase = KanaGamePhase.Results(newScore, allEntries.size, GameMode.MATCHING)) }
                            } else {
                                updateState {
                                    copy(phase = KanaGamePhase.Matching(
                                        cards = buildMatchCards(batches[nextBatch]),
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
                        val cur = uiState.value.phase as? KanaGamePhase.Matching ?: return@launch
                        updateState { copy(phase = cur.copy(wrongIndices = emptySet())) }
                    }
                }
            }
        }
    }

    // ── Typing ────────────────────────────────────────────────────────────────

    private fun startTyping() {
        if (allEntries.isEmpty()) return
        updateState { copy(phase = KanaGamePhase.Typing(entries = allEntries.shuffled())) }
    }

    private fun handleTypingInput(text: String) {
        val phase = uiState.value.phase as? KanaGamePhase.Typing ?: return
        if (phase.feedback != null) return
        updateState { copy(phase = phase.copy(inputText = text)) }
    }

    private fun handleTypingSubmit() {
        val phase = uiState.value.phase as? KanaGamePhase.Typing ?: return
        if (phase.feedback != null) return
        val correct = phase.inputText.trim().equals(phase.entries[phase.currentIndex].romaji, ignoreCase = true)
        val feedback = if (correct) TypingFeedback.CORRECT else TypingFeedback.WRONG
        updateState { copy(phase = phase.copy(feedback = feedback, score = if (correct) phase.score + 1 else phase.score)) }
        viewModelScope.launch {
            delay(900)
            val cur = uiState.value.phase as? KanaGamePhase.Typing ?: return@launch
            val next = cur.currentIndex + 1
            if (next >= cur.entries.size) {
                updateState { copy(phase = KanaGamePhase.Results(cur.score, cur.entries.size, GameMode.TYPING)) }
            } else {
                updateState { copy(phase = cur.copy(currentIndex = next, inputText = "", feedback = null)) }
            }
        }
    }

    // ── Flashcard ─────────────────────────────────────────────────────────────

    private fun startFlashcard() {
        if (allEntries.isEmpty()) return
        updateState { copy(phase = KanaGamePhase.Flashcard(entries = allEntries.shuffled())) }
    }

    private fun handleFlipFlashcard() {
        val phase = uiState.value.phase as? KanaGamePhase.Flashcard ?: return
        updateState { copy(phase = phase.copy(revealed = true)) }
    }

    private fun handleAnswerFlashcard(correct: Boolean) {
        val phase = uiState.value.phase as? KanaGamePhase.Flashcard ?: return
        val next = phase.currentIndex + 1
        val newCorrect = if (correct) phase.correctCount + 1 else phase.correctCount
        val newIncorrect = if (!correct) phase.incorrectCount + 1 else phase.incorrectCount
        if (next >= phase.entries.size) {
            updateState { copy(phase = KanaGamePhase.Results(newCorrect, phase.entries.size, GameMode.FLASHCARD)) }
        } else {
            updateState {
                copy(phase = phase.copy(
                    currentIndex = next,
                    revealed = false,
                    correctCount = newCorrect,
                    incorrectCount = newIncorrect,
                ))
            }
        }
    }

    // ── Multiple choice ───────────────────────────────────────────────────────

    private fun startMultipleChoice() {
        if (allEntries.isEmpty()) return
        val shuffled = allEntries.shuffled()
        updateState {
            copy(phase = KanaGamePhase.MultipleChoice(
                entries = shuffled,
                options = buildOptions(shuffled, 0),
            ))
        }
    }

    private fun buildOptions(entries: List<KanaEntry>, index: Int): List<String> {
        val correct = entries[index].romaji
        val distractors = allEntries
            .map { it.romaji }
            .filter { it != correct }
            .shuffled()
            .take(MULTI_CHOICE_OPTIONS - 1)
        return (distractors + correct).shuffled()
    }

    private fun handleSelectChoice(choice: String) {
        val phase = uiState.value.phase as? KanaGamePhase.MultipleChoice ?: return
        if (phase.selectedOption != null) return
        val correct = choice == phase.entries[phase.currentIndex].romaji
        val newScore = if (correct) phase.score + 1 else phase.score
        updateState { copy(phase = phase.copy(selectedOption = choice, isCorrect = correct, score = newScore)) }
        viewModelScope.launch {
            delay(900)
            val cur = uiState.value.phase as? KanaGamePhase.MultipleChoice ?: return@launch
            val next = cur.currentIndex + 1
            if (next >= cur.entries.size) {
                updateState { copy(phase = KanaGamePhase.Results(cur.score, cur.entries.size, GameMode.MULTIPLE_CHOICE)) }
            } else {
                updateState {
                    copy(phase = cur.copy(
                        currentIndex = next,
                        options = buildOptions(cur.entries, next),
                        selectedOption = null,
                        isCorrect = null,
                    ))
                }
            }
        }
    }

    // ── Kana speed ────────────────────────────────────────────────────────────

    private fun startKanaSpeed() {
        if (allEntries.isEmpty()) return
        updateState { copy(phase = KanaGamePhase.KanaSpeed(entries = allEntries.shuffled())) }
        launchSpeedTimer()
    }

    private fun launchSpeedTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (tick in SPEED_TICKS downTo 0) {
                delay(SPEED_TICK_MS)
                val phase = uiState.value.phase as? KanaGamePhase.KanaSpeed ?: return@launch
                if (phase.feedback != null) return@launch
                val fraction = tick.toFloat() / SPEED_TICKS.toFloat()
                if (tick == 0) {
                    dispatchAction(KanaGameAction.SpeedTimerExpired)
                } else {
                    dispatchAction(KanaGameAction.SpeedTimerUpdate(fraction))
                }
            }
        }
    }

    private fun handleSpeedTimerUpdate(fraction: Float) {
        val phase = uiState.value.phase as? KanaGamePhase.KanaSpeed ?: return
        if (phase.feedback != null) return
        updateState { copy(phase = phase.copy(timeRemaining = fraction)) }
    }

    private fun handleSpeedTimerExpired() {
        val phase = uiState.value.phase as? KanaGamePhase.KanaSpeed ?: return
        if (phase.feedback != null) return
        updateState { copy(phase = phase.copy(feedback = TypingFeedback.WRONG, timeRemaining = 0f)) }
        viewModelScope.launch {
            delay(900)
            advanceSpeed()
        }
    }

    private fun handleSpeedInput(text: String) {
        val phase = uiState.value.phase as? KanaGamePhase.KanaSpeed ?: return
        if (phase.feedback != null) return
        updateState { copy(phase = phase.copy(inputText = text)) }
    }

    private fun handleSpeedSubmit() {
        val phase = uiState.value.phase as? KanaGamePhase.KanaSpeed ?: return
        if (phase.feedback != null) return
        timerJob?.cancel()
        val correct = phase.inputText.trim().equals(phase.entries[phase.currentIndex].romaji, ignoreCase = true)
        val feedback = if (correct) TypingFeedback.CORRECT else TypingFeedback.WRONG
        updateState { copy(phase = phase.copy(feedback = feedback, score = if (correct) phase.score + 1 else phase.score)) }
        viewModelScope.launch {
            delay(900)
            advanceSpeed()
        }
    }

    private fun advanceSpeed() {
        val phase = uiState.value.phase as? KanaGamePhase.KanaSpeed ?: return
        val next = phase.currentIndex + 1
        if (next >= phase.entries.size) {
            updateState { copy(phase = KanaGamePhase.Results(phase.score, phase.entries.size, GameMode.KANA_SPEED)) }
        } else {
            updateState { copy(phase = phase.copy(currentIndex = next, inputText = "", feedback = null, timeRemaining = 1f)) }
            launchSpeedTimer()
        }
    }
}
