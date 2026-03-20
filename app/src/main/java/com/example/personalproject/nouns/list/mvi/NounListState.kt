package com.example.personalproject.nouns.list.mvi

import com.example.personalproject.data.model.NounEntry
import com.example.personalproject.mvi.BaseState

data class NounListState(
    val allEntries: List<NounEntry> = emptyList(),
    val displayedEntries: List<NounEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedJlptLevel: String? = null,
    val availableJlptLevels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
