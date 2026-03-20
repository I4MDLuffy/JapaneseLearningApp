package com.example.personalproject.kanji.list.mvi

import com.example.personalproject.data.model.KanjiEntry
import com.example.personalproject.mvi.BaseState

data class KanjiListState(
    val allEntries: List<KanjiEntry> = emptyList(),
    val displayedEntries: List<KanjiEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedJlptLevel: String? = null,
    val availableJlptLevels: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
