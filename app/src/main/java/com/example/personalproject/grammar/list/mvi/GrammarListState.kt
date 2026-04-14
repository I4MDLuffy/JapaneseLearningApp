package app.kotori.japanese.grammar.list.mvi

import app.kotori.japanese.data.model.GrammarEntry
import app.kotori.japanese.mvi.BaseState

data class GrammarListState(
    val allEntries: List<GrammarEntry> = emptyList(),
    val displayedEntries: List<GrammarEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedJlptLevel: String? = null,
    val availableJlptLevels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
