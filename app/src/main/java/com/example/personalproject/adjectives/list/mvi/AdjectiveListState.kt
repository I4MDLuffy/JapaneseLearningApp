package app.kotori.japanese.adjectives.list.mvi

import app.kotori.japanese.data.model.AdjectiveEntry
import app.kotori.japanese.mvi.BaseState

data class AdjectiveListState(
    val allEntries: List<AdjectiveEntry> = emptyList(),
    val displayedEntries: List<AdjectiveEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedJlptLevel: String? = null,
    val availableJlptLevels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
