package app.kotori.japanese.ui.games.mvi

import androidx.lifecycle.viewModelScope
import app.kotori.japanese.AppContainer
import app.kotori.japanese.data.model.AdjectiveEntry
import app.kotori.japanese.data.model.KanjiEntry
import app.kotori.japanese.data.model.NounEntry
import app.kotori.japanese.data.model.PhraseEntry
import app.kotori.japanese.data.model.VerbEntry
import app.kotori.japanese.data.model.VocabularyWord
import app.kotori.japanese.mvi.BaseViewModel
import app.kotori.japanese.ui.games.HiraganaUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MATCH_BATCH_SIZE = 12
private const val MULTI_OPTIONS = 4
private const val QUIZ_TICKS = 50   // 50 × 100 ms = 5 s
private const val SPEED_TICKS = 60  // 60 × 100 ms = 6 s
private const val TICK_MS = 100L
private const val SWIPE_TILE_COUNT = 8

class StudyGameViewModel(
    private val gameType: GameType,
    private val setKey: String,
    private val container: AppContainer,
) : BaseViewModel<StudyGameState, StudyGameAction>(StudyGameState()) {

    private var timerJob: Job? = null
    private var allItems: List<StudyItem> = emptyList()
    private var batches: List<List<StudyItem>> = emptyList()

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

            // Fill in the Blank
            is StudyGameAction.SetFillBlankDirection -> handleSetFillBlankDirection(action.direction)
            is StudyGameAction.UpdateFillBlankInput -> handleUpdateFillBlankInput(action.text)
            is StudyGameAction.SubmitFillBlank -> handleSubmitFillBlank()
            is StudyGameAction.NextFillBlank -> handleNextFillBlank()
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    private fun loadAndStart() {
        viewModelScope.launch {
            updateState { copy(phase = StudyGamePhase.Loading, error = null) }
            try {
                val items = loadItems()
                allItems = items
                batches = items.shuffled().chunked(MATCH_BATCH_SIZE)
                startGame(items.shuffled())
            } catch (e: Exception) {
                updateState { copy(error = e.message ?: "Failed to load") }
            }
        }
    }

    private suspend fun loadItems(): List<StudyItem> = when {
        setKey == "saved_vocabulary" || setKey == "saved_all" -> {
            val ids = container.savedRepository.getSavedItemIds("vocabulary")
            ids.mapNotNull { container.vocabularyRepository.getWordById(it) }.map { it.toStudyItem() }
        }
        setKey.startsWith("ids:") -> {
            // Hand-picked items: "ids:TYPE:id1,id2,id3"
            val parts = setKey.split(":")
            val type = parts.getOrElse(1) { "vocab" }
            val ids = parts.getOrElse(2) { "" }.split(",").filter { it.isNotBlank() }
            loadPickedItems(type, ids)
        }
        setKey.startsWith("browse:") -> {
            val parts = setKey.split(":")
            val contentType = parts.getOrElse(1) { "vocab" }
            val jlpt = parts.getOrElse(2) { "all" }
            loadBrowseItems(contentType, jlpt)
        }
        else -> {
            // Legacy: "beginner_vocab_0", "intermediate_vocab_2", etc.
            val parts = setKey.split("_")
            val jlpt = when (parts.getOrNull(0)) {
                "beginner" -> "N5"
                "intermediate" -> "N4"
                "advanced" -> "N3"
                "master" -> "N2"
                else -> "N5"
            }
            val setIndex = parts.getOrNull(2)?.toIntOrNull() ?: 0
            container.vocabularyRepository.filterByJlpt(jlpt)
                .sortedBy { it.id }
                .chunked(5)
                .getOrElse(setIndex) { emptyList() }
                .map { it.toStudyItem() }
        }
    }

    private suspend fun loadBrowseItems(type: String, jlpt: String): List<StudyItem> = when (type) {
        "vocab" -> {
            val words = if (jlpt == "all") container.vocabularyRepository.getAllWords()
                        else container.vocabularyRepository.filterByJlpt(jlpt)
            words.map { it.toStudyItem() }
        }
        "kanji" -> {
            val items = if (jlpt == "all") container.kanjiRepository.getAllKanji()
                        else container.kanjiRepository.filterByJlpt(jlpt)
            items.map { it.toStudyItem() }
        }
        "verb" -> {
            val items = if (jlpt == "all") container.verbRepository.getAllVerbs()
                        else container.verbRepository.filterByJlpt(jlpt)
            items.map { it.toStudyItem() }
        }
        "adjective" -> {
            val items = if (jlpt == "all") container.adjectiveRepository.getAllAdjectives()
                        else container.adjectiveRepository.filterByJlpt(jlpt)
            items.map { it.toStudyItem() }
        }
        "noun" -> {
            val items = if (jlpt == "all") container.nounRepository.getAllNouns()
                        else container.nounRepository.filterByJlpt(jlpt)
            items.map { it.toStudyItem() }
        }
        "phrase" -> {
            val items = if (jlpt == "all") container.phraseRepository.getAllPhrases()
                        else container.phraseRepository.filterByJlpt(jlpt)
            items.map { it.toStudyItem() }
        }
        else -> emptyList()
    }

    private fun startGame(items: List<StudyItem>) {
        timerJob?.cancel()
        when (gameType) {
            GameType.FLASHCARDS -> updateState {
                copy(phase = StudyGamePhase.Flashcard(items))
            }
            GameType.TIMED_QUIZ -> {
                updateState {
                    copy(phase = StudyGamePhase.TimedQuiz(
                        entries = items,
                        options = buildOptions(items, 0),
                    ))
                }
                launchQuizTimer()
            }
            GameType.MATCH_PAIRS -> {
                val mode = uiState.value.pairMode
                updateState {
                    copy(phase = StudyGamePhase.MatchPairs(
                        entries = items,
                        cards = buildMatchCards(batches.getOrElse(0) { items.take(MATCH_BATCH_SIZE) }, mode),
                        totalBatches = batches.size,
                    ))
                }
            }
            GameType.KANA_SPEED -> {
                val speedItems = items.filter { it.reading.isNotBlank() }.ifEmpty { items }
                val first = speedItems.getOrNull(0)
                updateState {
                    copy(phase = StudyGamePhase.KanaSpeed(
                        entries = speedItems,
                        gridTiles = if (first != null) HiraganaUtils.buildGrid(first.reading) else emptyList(),
                    ))
                }
                launchSpeedTimer()
            }
            GameType.KANA_SWIPE -> {
                val swipeItems = items
                    .filter { it.reading.isNotBlank() && HiraganaUtils.decompose(it.reading).size <= 6 }
                    .ifEmpty { items.filter { it.reading.isNotBlank() }.ifEmpty { items } }
                val first = swipeItems.getOrNull(0)
                updateState {
                    copy(phase = StudyGamePhase.KanaSwipe(
                        entries = swipeItems,
                        tiles = if (first != null) HiraganaUtils.buildGrid(first.reading, SWIPE_TILE_COUNT) else emptyList(),
                    ))
                }
            }
            GameType.FILL_BLANK -> updateState {
                copy(phase = StudyGamePhase.FillBlank(entries = items))
            }
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

    private fun buildOptions(entries: List<StudyItem>, index: Int): List<String> {
        val correct = entries[index].answer
        val distractors = allItems.map { it.answer }
            .filter { it != correct }
            .shuffled()
            .take(MULTI_OPTIONS - 1)
        return (distractors + correct).shuffled()
    }

    private fun handleSelectOption(option: String) {
        val phase = uiState.value.phase as? StudyGamePhase.TimedQuiz ?: return
        if (phase.selectedOption != null) return
        timerJob?.cancel()
        val correct = option == phase.entries[phase.currentIndex].answer
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

    private fun buildMatchCards(entries: List<StudyItem>, mode: PairMode): List<StudyMatchCard> =
        entries.flatMap { item ->
            listOf(
                StudyMatchCard(pairId = item.id, text = item.question, isJapanese = true),
                StudyMatchCard(pairId = item.id, text = if (mode == PairMode.ENGLISH) item.answer else item.romaji, isJapanese = false),
            )
        }.shuffled()

    private fun handleSetPairMode(mode: PairMode) {
        val phase = uiState.value.phase as? StudyGamePhase.MatchPairs ?: return
        val currentBatchItems = batches.getOrElse(phase.batchIndex) { phase.entries.take(MATCH_BATCH_SIZE) }
        updateState {
            copy(
                pairMode = mode,
                phase = phase.copy(
                    cards = buildMatchCards(currentBatchItems, mode),
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
                                updateState { copy(phase = StudyGamePhase.Results(newScore, allItems.size, GameType.MATCH_PAIRS)) }
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
        val target = HiraganaUtils.decompose(phase.entries[phase.currentIndex].reading)
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
            val nextItem = phase.entries[next]
            updateState {
                copy(phase = phase.copy(
                    currentIndex = next,
                    gridTiles = HiraganaUtils.buildGrid(nextItem.reading),
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
        if (phase.path.contains(index)) return
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
        val item = phase.entries.getOrNull(phase.currentIndex) ?: return
        val target = HiraganaUtils.decompose(item.reading)
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
            val nextItem = phase.entries[next]
            updateState {
                copy(phase = phase.copy(
                    currentIndex = next,
                    tiles = HiraganaUtils.buildGrid(nextItem.reading, SWIPE_TILE_COUNT),
                    path = emptyList(),
                    feedback = null,
                ))
            }
        }
    }

    // ── Picked items ──────────────────────────────────────────────────────────

    private suspend fun loadPickedItems(type: String, ids: List<String>): List<StudyItem> = when (type) {
        "vocab" -> ids.mapNotNull { container.vocabularyRepository.getWordById(it) }.map { it.toStudyItem() }
        "kanji" -> ids.mapNotNull { container.kanjiRepository.getKanjiById(it) }.map { it.toStudyItem() }
        "verb" -> ids.mapNotNull { container.verbRepository.getVerbById(it) }.map { it.toStudyItem() }
        "adjective" -> ids.mapNotNull { container.adjectiveRepository.getAdjectiveById(it) }.map { it.toStudyItem() }
        "noun" -> ids.mapNotNull { container.nounRepository.getNounById(it) }.map { it.toStudyItem() }
        "phrase" -> ids.mapNotNull { container.phraseRepository.getPhraseById(it) }.map { it.toStudyItem() }
        else -> emptyList()
    }

    // ── Fill in the Blank ─────────────────────────────────────────────────────

    private fun handleSetFillBlankDirection(direction: FillBlankDirection) {
        val phase = uiState.value.phase as? StudyGamePhase.FillBlank ?: return
        updateState { copy(phase = phase.copy(direction = direction, inputText = "", isSubmitted = false, isCorrect = null)) }
    }

    private fun handleUpdateFillBlankInput(text: String) {
        val phase = uiState.value.phase as? StudyGamePhase.FillBlank ?: return
        if (!phase.isSubmitted) updateState { copy(phase = phase.copy(inputText = text)) }
    }

    private fun handleSubmitFillBlank() {
        val phase = uiState.value.phase as? StudyGamePhase.FillBlank ?: return
        if (phase.isSubmitted || phase.inputText.isBlank()) return
        val item = phase.entries.getOrNull(phase.currentIndex) ?: return
        val typed = phase.inputText.trim().lowercase()
        val isCorrect = when (phase.direction) {
            FillBlankDirection.JP_TO_EN -> typed == item.answer.trim().lowercase()
            FillBlankDirection.EN_TO_JP -> typed == item.reading.trim().lowercase() || typed == item.romaji.trim().lowercase()
        }
        val newScore = if (isCorrect) phase.score + 1 else phase.score
        updateState { copy(phase = phase.copy(isSubmitted = true, isCorrect = isCorrect, score = newScore)) }
    }

    private fun handleNextFillBlank() {
        val phase = uiState.value.phase as? StudyGamePhase.FillBlank ?: return
        val next = phase.currentIndex + 1
        if (next >= phase.entries.size) {
            updateState { copy(phase = StudyGamePhase.Results(phase.score, phase.entries.size, GameType.FILL_BLANK)) }
        } else {
            updateState { copy(phase = phase.copy(currentIndex = next, inputText = "", isSubmitted = false, isCorrect = null)) }
        }
    }
}

// ── Extension functions: data model → StudyItem ───────────────────────────────

private fun VocabularyWord.toStudyItem() = StudyItem(
    id = id,
    type = "vocab",
    question = japanese,
    reading = hiragana,
    romaji = romaji,
    answer = english,
    jlptLevel = jlptLevel,
)

private fun KanjiEntry.toStudyItem() = StudyItem(
    id = id,
    type = "kanji",
    question = kanji,
    reading = hiragana,
    romaji = hiragana,
    answer = meaning,
    jlptLevel = jlptLevel,
)

private fun VerbEntry.toStudyItem() = StudyItem(
    id = id,
    type = "verb",
    question = kanji.ifBlank { dictionaryForm },
    reading = dictionaryForm,
    romaji = romaji,
    answer = meaning,
    jlptLevel = jlptLevel,
)

private fun AdjectiveEntry.toStudyItem() = StudyItem(
    id = id,
    type = "adjective",
    question = kanji.ifBlank { hiragana },
    reading = hiragana,
    romaji = romaji,
    answer = meaning,
    jlptLevel = jlptLevel,
)

private fun NounEntry.toStudyItem() = StudyItem(
    id = id,
    type = "noun",
    question = kanji.ifBlank { hiragana },
    reading = hiragana,
    romaji = romaji,
    answer = meaning,
    jlptLevel = jlptLevel,
)

private fun PhraseEntry.toStudyItem() = StudyItem(
    id = id,
    type = "phrase",
    question = phrase,
    reading = reading,
    romaji = romaji,
    answer = meaning,
    jlptLevel = jlptLevel,
)
