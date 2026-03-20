package com.example.personalproject.modules.detail.mvi

import com.example.personalproject.data.model.LearningModule
import com.example.personalproject.mvi.BaseState

data class ModuleDetailState(
    val module: LearningModule? = null,
    val expandedLessonId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
