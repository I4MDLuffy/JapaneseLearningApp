package app.kotori.japanese.kanji.list.mvi

import app.kotori.japanese.data.model.KanjiEntry
import app.kotori.japanese.mvi.BaseState

data class KanjiListState(
    val allEntries: List<KanjiEntry> = emptyList(),
    val displayedEntries: List<KanjiEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedJlptLevel: String? = null,
    val availableJlptLevels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
