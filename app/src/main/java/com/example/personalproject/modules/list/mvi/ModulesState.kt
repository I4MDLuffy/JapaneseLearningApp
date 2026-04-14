package app.kotori.japanese.modules.list.mvi

import app.kotori.japanese.data.model.LearningModule
import app.kotori.japanese.data.model.ModuleCategory
import app.kotori.japanese.mvi.BaseState

data class ModulesState(
    val allModules: List<LearningModule> = emptyList(),
    val displayedModules: List<LearningModule> = emptyList(),
    val selectedCategory: ModuleCategory? = null,
    val isLoading: Boolean = false,
) : BaseState
