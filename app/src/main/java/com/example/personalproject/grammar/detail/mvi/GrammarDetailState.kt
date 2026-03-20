package com.example.personalproject.grammar.detail.mvi

import com.example.personalproject.data.model.GrammarEntry
import com.example.personalproject.mvi.BaseState

data class GrammarDetailState(
    val entry: GrammarEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
