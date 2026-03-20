package com.example.personalproject.vocabulary.detail.mvi

import com.example.personalproject.data.model.VocabularyWord
import com.example.personalproject.mvi.BaseState

data class VocabularyDetailState(
    val word: VocabularyWord? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
