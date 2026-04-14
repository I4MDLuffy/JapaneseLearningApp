package app.kotori.japanese.nouns.list.mvi

import app.kotori.japanese.data.model.NounEntry
import app.kotori.japanese.mvi.BaseState

data class NounListState(
    val allEntries: List<NounEntry> = emptyList(),
    val displayedEntries: List<NounEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedJlptLevel: String? = null,
    val availableJlptLevels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
