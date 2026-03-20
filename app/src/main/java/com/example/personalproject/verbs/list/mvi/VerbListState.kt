package com.example.personalproject.verbs.list.mvi

import com.example.personalproject.data.model.VerbEntry
import com.example.personalproject.mvi.BaseState

data class VerbListState(
    val allEntries: List<VerbEntry> = emptyList(),
    val displayedEntries: List<VerbEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedJlptLevel: String? = null,
    val availableJlptLevels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
