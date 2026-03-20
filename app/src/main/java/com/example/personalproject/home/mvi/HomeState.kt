package com.example.personalproject.home.mvi

import com.example.personalproject.data.model.VocabularyWord
import com.example.personalproject.mvi.BaseState

data class HomeState(
    val wordOfTheDay: VocabularyWord? = null,
    val totalWords: Int = 0,
    val totalModules: Int = 0,
    val isLoading: Boolean = false,
) : BaseState
