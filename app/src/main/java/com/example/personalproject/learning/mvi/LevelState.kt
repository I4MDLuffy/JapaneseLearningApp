package com.example.personalproject.learning.mvi

import com.example.personalproject.data.model.Chapter
import com.example.personalproject.mvi.BaseState

data class LevelState(
    val levelName: String = "",
    val chapters: List<Chapter> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
) : BaseState
