package com.example.personalproject.adjectives.list.mvi

import com.example.personalproject.data.model.AdjectiveEntry
import com.example.personalproject.mvi.BaseState

data class AdjectiveListState(
    val allEntries: List<AdjectiveEntry> = emptyList(),
    val displayedEntries: List<AdjectiveEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedJlptLevel: String? = null,
    val availableJlptLevels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
