package app.kotori.japanese.modules.detail.mvi

import app.kotori.japanese.data.model.LearningModule
import app.kotori.japanese.mvi.BaseState

data class ModuleDetailState(
    val module: LearningModule? = null,
    val expandedLessonId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
