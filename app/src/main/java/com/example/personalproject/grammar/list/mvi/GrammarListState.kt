package com.example.personalproject.grammar.list.mvi

import com.example.personalproject.data.model.GrammarEntry
import com.example.personalproject.mvi.BaseState

data class GrammarListState(
    val allEntries: List<GrammarEntry> = emptyList(),
    val displayedEntries: List<GrammarEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedJlptLevel: String? = null,
    val availableJlptLevels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
