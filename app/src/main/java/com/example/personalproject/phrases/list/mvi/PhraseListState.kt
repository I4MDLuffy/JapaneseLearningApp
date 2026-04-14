package app.kotori.japanese.phrases.list.mvi

import app.kotori.japanese.data.model.PhraseEntry
import app.kotori.japanese.mvi.BaseState

data class PhraseListState(
    val allEntries: List<PhraseEntry> = emptyList(),
    val displayedEntries: List<PhraseEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedJlptLevel: String? = null,
    val availableJlptLevels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
