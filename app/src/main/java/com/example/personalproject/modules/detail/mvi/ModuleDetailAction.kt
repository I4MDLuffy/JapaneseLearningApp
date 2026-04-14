package app.kotori.japanese.modules.detail.mvi

import app.kotori.japanese.mvi.BaseAction

sealed class ModuleDetailAction : BaseAction {
    data class LoadModule(val id: String) : ModuleDetailAction()
    data class ToggleLesson(val lessonId: String) : ModuleDetailAction()
}
