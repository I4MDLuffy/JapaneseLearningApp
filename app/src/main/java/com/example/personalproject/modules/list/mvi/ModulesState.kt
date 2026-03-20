package com.example.personalproject.modules.list.mvi

import com.example.personalproject.data.model.LearningModule
import com.example.personalproject.data.model.ModuleCategory
import com.example.personalproject.mvi.BaseState

data class ModulesState(
    val allModules: List<LearningModule> = emptyList(),
    val displayedModules: List<LearningModule> = emptyList(),
    val selectedCategory: ModuleCategory? = null,
    val isLoading: Boolean = false,
) : BaseState
