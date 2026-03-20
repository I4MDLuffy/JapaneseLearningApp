package com.example.personalproject.modules.detail.mvi

import com.example.personalproject.mvi.BaseAction

sealed class ModuleDetailAction : BaseAction {
    data class LoadModule(val id: String) : ModuleDetailAction()
    data class ToggleLesson(val lessonId: String) : ModuleDetailAction()
}
