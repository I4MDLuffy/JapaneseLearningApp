package com.example.personalproject.kanji.detail.mvi

import com.example.personalproject.data.model.KanjiEntry
import com.example.personalproject.mvi.BaseState

data class KanjiDetailState(
    val entry: KanjiEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
