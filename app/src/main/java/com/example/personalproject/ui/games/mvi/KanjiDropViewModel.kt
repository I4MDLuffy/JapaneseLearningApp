package app.kotori.japanese.ui.games.mvi

import androidx.lifecycle.viewModelScope
import app.kotori.japanese.data.model.KanjiEntry
import app.kotori.japanese.data.repository.KanjiRepository
import app.kotori.japanese.mvi.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DROP_TICKS = 60      // 60 × 100 ms = 6 s per kanji
private const val DROP_TICK_MS = 100L
private const val KANJI_PER_GAME = 10
private const val DROP_OPTIONS = 4

class KanjiDropViewModel(
    private val kanjiRepository: KanjiRepository,
    private val jlptFilter: String = "all",  // "all" | "N5" | "N4" | "N3" | "N2"
) : BaseViewModel<KanjiDropState, KanjiDropAction>(KanjiDropState()) {

    private var timerJob: Job? = null
    private var allKanji: List<KanjiEntry> = emptyList()

    init { loadKanji() }

    private fun loadKanji() {
        execute(
            block = { kanjiRepository.getAllKanji() },
            onSuccess = { all ->
                allKanji = all
                val pool = if (jlptFilter == "all") all else all.filter { it.jlptLevel == jlptFilter }
                val entries = pool.filter { it.hiragana.isNotBlank() }.shuffled().take(KANJI_PER_GAME)
                if (entries.isEmpty()) {
                    updateState { copy(phase = KanjiDropPhase.Results(0, 0)) }
                } else {
                    updateState {
                        copy(phase = KanjiDropPhase.Playing(
                            entries = entries,
                            options = buildOptions(entries, 0, mode),
                        ))
                    }
                    launchDropTimer()
                }
            },
        )
    }

    override fun dispatchAction(action: KanjiDropAction) {
        when (action) {
            is KanjiDropAction.SetMode -> handleSetMode(action.mode)
            is KanjiDropAction.SelectAnswer -> handleSelect(action.option)
            is KanjiDropAction.DropTimerTick -> handleTimerTick(action.progress)
            is KanjiDropAction.DropTimerExpired -> handleTimerExpired()
            is KanjiDropAction.Restart -> { timerJob?.cancel(); loadKanji() }
        }
    }

    private fun handleSetMode(mode: KanjiDropMode) {
        val phase = uiState.value.phase as? KanjiDropPhase.Playing ?: return
        updateState {
            copy(
                mode = mode,
                phase = phase.copy(options = buildOptions(phase.entries, phase.currentIndex, mode)),
            )
        }
    }

    private fun handleSelect(option: String) {
        val state = uiState.value
        val phase = state.phase as? KanjiDropPhase.Playing ?: return
        if (phase.selectedOption != null) return
        timerJob?.cancel()
        val correct = optionText(phase.entries[phase.currentIndex], state.mode)
        val newScore = if (option == correct) phase.score + 1 else phase.score
        updateState { copy(phase = phase.copy(selectedOption = option, score = newScore)) }
        viewModelScope.launch { delay(900); advance() }
    }

    private fun handleTimerTick(progress: Float) {
        val phase = uiState.value.phase as? KanjiDropPhase.Playing ?: return
        if (phase.selectedOption != null) return
        updateState { copy(phase = phase.copy(dropProgress = progress)) }
    }

    private fun handleTimerExpired() {
        val phase = uiState.value.phase as? KanjiDropPhase.Playing ?: return
        if (phase.selectedOption != null) return
        val newLives = phase.lives - 1
        // selectedOption = "" signals "time out" (distinct from null = no answer yet)
        updateState { copy(phase = phase.copy(selectedOption = "", dropProgress = 1f, lives = newLives)) }
        viewModelScope.launch {
            delay(900)
            if (newLives <= 0) {
                val cur = uiState.value.phase as? KanjiDropPhase.Playing ?: return@launch
                updateState { copy(phase = KanjiDropPhase.Results(cur.score, cur.entries.size)) }
            } else {
                advance()
            }
        }
    }

    private fun advance() {
        val state = uiState.value
        val phase = state.phase as? KanjiDropPhase.Playing ?: return
        val next = phase.currentIndex + 1
        if (next >= phase.entries.size) {
            updateState { copy(phase = KanjiDropPhase.Results(phase.score, phase.entries.size)) }
        } else {
            updateState {
                copy(phase = phase.copy(
                    currentIndex = next,
                    dropProgress = 0f,
                    options = buildOptions(phase.entries, next, state.mode),
                    selectedOption = null,
                ))
            }
            launchDropTimer()
        }
    }

    private fun launchDropTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (tick in 0..DROP_TICKS) {
                delay(DROP_TICK_MS)
                val phase = uiState.value.phase as? KanjiDropPhase.Playing ?: return@launch
                if (phase.selectedOption != null) return@launch
                val progress = tick.toFloat() / DROP_TICKS.toFloat()
                if (tick == DROP_TICKS) dispatchAction(KanjiDropAction.DropTimerExpired)
                else dispatchAction(KanjiDropAction.DropTimerTick(progress))
            }
        }
    }

    private fun buildOptions(entries: List<KanjiEntry>, index: Int, mode: KanjiDropMode): List<String> {
        val correct = optionText(entries[index], mode)
        val pool = if (allKanji.size >= DROP_OPTIONS) allKanji else entries
        val distractors = pool
            .filter { it.hiragana.isNotBlank() }
            .map { optionText(it, mode) }
            .filter { it != correct }
            .shuffled()
            .take(DROP_OPTIONS - 1)
        return (distractors + correct).shuffled()
    }

    private fun optionText(entry: KanjiEntry, mode: KanjiDropMode): String = when (mode) {
        KanjiDropMode.HIRAGANA -> entry.hiragana
        KanjiDropMode.ROMAJI -> entry.kunYomi.firstOrNull()?.takeIf { it.isNotBlank() } ?: entry.hiragana
    }
}
