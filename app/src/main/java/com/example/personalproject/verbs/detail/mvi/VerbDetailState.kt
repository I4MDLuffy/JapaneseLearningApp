package com.example.personalproject.verbs.detail.mvi

import com.example.personalproject.data.model.VerbEntry
import com.example.personalproject.mvi.BaseState

data class VerbDetailState(
    val entry: VerbEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
