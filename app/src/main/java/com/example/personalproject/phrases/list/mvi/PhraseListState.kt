package com.example.personalproject.phrases.list.mvi

import com.example.personalproject.data.model.PhraseEntry
import com.example.personalproject.mvi.BaseState

data class PhraseListState(
    val allEntries: List<PhraseEntry> = emptyList(),
    val displayedEntries: List<PhraseEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedJlptLevel: String? = null,
    val availableJlptLevels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
