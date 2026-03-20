package com.example.personalproject.adjectives.detail.mvi

import com.example.personalproject.data.model.AdjectiveEntry
import com.example.personalproject.mvi.BaseState

data class AdjectiveDetailState(
    val entry: AdjectiveEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
