package app.kotori.japanese.verbs.list.mvi

import app.kotori.japanese.data.model.VerbEntry
import app.kotori.japanese.mvi.BaseState

data class VerbListState(
    val allEntries: List<VerbEntry> = emptyList(),
    val displayedEntries: List<VerbEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedJlptLevel: String? = null,
    val availableJlptLevels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
