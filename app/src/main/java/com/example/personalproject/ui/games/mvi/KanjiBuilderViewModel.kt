package app.kotori.japanese.ui.games.mvi

import androidx.lifecycle.viewModelScope
import app.kotori.japanese.data.model.KanjiEntry
import app.kotori.japanese.data.model.RadicalEntry
import app.kotori.japanese.data.repository.KanjiRepository
import app.kotori.japanese.data.repository.RadicalRepository
import app.kotori.japanese.mvi.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val BUILDER_GAME_SIZE = 10
private const val BUILDER_TILE_COUNT = 9

class KanjiBuilderViewModel(
    private val kanjiRepository: KanjiRepository,
    private val radicalRepository: RadicalRepository,
    private val jlptFilter: String = "all",  // "all" | "N5" | "N4" | "N3" | "N2"
) : BaseViewModel<KanjiBuilderState, KanjiBuilderAction>(KanjiBuilderState()) {

    private var allRadicals: List<RadicalEntry> = emptyList()

    init { loadData() }

    private fun loadData() {
        execute(
            block = {
                val radicals = radicalRepository.getAllRadicals()
                val all = kanjiRepository.getAllKanji()
                val pool = if (jlptFilter == "all") all else all.filter { it.jlptLevel == jlptFilter }
                val kanji = pool
                    .filter { it.radicalReferences.size >= 2 }
                    .shuffled()
                    .take(BUILDER_GAME_SIZE)
                Pair(radicals, kanji)
            },
            onSuccess = { (radicals, kanji) ->
                allRadicals = radicals
                if (kanji.isEmpty()) {
                    updateState { copy(phase = KanjiBuilderPhase.Results(0, 0)) }
                } else {
                    val first = kanji[0]
                    updateState {
                        copy(phase = KanjiBuilderPhase.Building(
                            entries = kanji,
                            tiles = buildTiles(first),
                            correctIds = first.radicalReferences.toSet(),
                        ))
                    }
                }
            },
        )
    }

    override fun dispatchAction(action: KanjiBuilderAction) {
        when (action) {
            is KanjiBuilderAction.TapTile -> handleTap(action.radicalId)
            is KanjiBuilderAction.Restart -> loadData()
        }
    }

    private fun handleTap(radicalId: String) {
        val phase = uiState.value.phase as? KanjiBuilderPhase.Building ?: return
        if (phase.selectedIds.contains(radicalId) || phase.wrongIds.contains(radicalId)) return

        if (phase.correctIds.contains(radicalId)) {
            val newSelected = phase.selectedIds + radicalId
            updateState { copy(phase = phase.copy(selectedIds = newSelected)) }
            if (newSelected == phase.correctIds) {
                val newScore = phase.score + 1
                updateState { copy(phase = phase.copy(score = newScore)) }
                viewModelScope.launch { delay(800); advance(newScore) }
            }
        } else {
            updateState { copy(phase = phase.copy(wrongIds = phase.wrongIds + radicalId)) }
            viewModelScope.launch {
                delay(500)
                val cur = uiState.value.phase as? KanjiBuilderPhase.Building ?: return@launch
                updateState { copy(phase = cur.copy(wrongIds = cur.wrongIds - radicalId)) }
            }
        }
    }

    private fun advance(score: Int) {
        val phase = uiState.value.phase as? KanjiBuilderPhase.Building ?: return
        val next = phase.currentIndex + 1
        if (next >= phase.entries.size) {
            updateState { copy(phase = KanjiBuilderPhase.Results(score, phase.entries.size)) }
        } else {
            val nextKanji = phase.entries[next]
            updateState {
                copy(phase = phase.copy(
                    currentIndex = next,
                    tiles = buildTiles(nextKanji),
                    correctIds = nextKanji.radicalReferences.toSet(),
                    selectedIds = emptySet(),
                    wrongIds = emptySet(),
                    score = score,
                ))
            }
        }
    }

    private fun buildTiles(kanji: KanjiEntry): List<KanjiBuilderTile> {
        val correctRadicals = kanji.radicalReferences.mapNotNull { id ->
            allRadicals.find { it.id == id }
        }
        val distractorCount = (BUILDER_TILE_COUNT - correctRadicals.size).coerceAtLeast(0)
        val distractors = allRadicals
            .filter { it.id !in kanji.radicalReferences }
            .shuffled()
            .take(distractorCount)
        return (correctRadicals + distractors)
            .shuffled()
            .map { KanjiBuilderTile(radicalId = it.id, character = it.character, meaning = it.meaning) }
    }
}
