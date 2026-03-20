package com.example.personalproject.nouns.detail.mvi

import com.example.personalproject.data.model.NounEntry
import com.example.personalproject.mvi.BaseState

data class NounDetailState(
    val entry: NounEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
