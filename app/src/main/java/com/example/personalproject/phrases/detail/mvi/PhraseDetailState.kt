package com.example.personalproject.phrases.detail.mvi

import com.example.personalproject.data.model.PhraseEntry
import com.example.personalproject.mvi.BaseState

data class PhraseDetailState(
    val entry: PhraseEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
