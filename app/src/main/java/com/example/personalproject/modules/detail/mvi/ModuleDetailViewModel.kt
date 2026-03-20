package com.example.personalproject.modules.detail.mvi

import com.example.personalproject.data.repository.ModuleRepository
import com.example.personalproject.mvi.BaseViewModel

class ModuleDetailViewModel(
    private val repository: ModuleRepository,
    private val moduleId: String,
) : BaseViewModel<ModuleDetailState, ModuleDetailAction>(ModuleDetailState()) {

    init {
        dispatchAction(ModuleDetailAction.LoadModule(moduleId))
    }

    override fun dispatchAction(action: ModuleDetailAction) {
        when (action) {
            is ModuleDetailAction.LoadModule -> loadModule(action.id)
            is ModuleDetailAction.ToggleLesson -> toggleLesson(action.lessonId)
        }
    }

    private fun loadModule(id: String) {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getModuleById(id) },
            onSuccess = { module ->
                updateState {
                    copy(
                        module = module,
                        // Auto-expand the first lesson
                        expandedLessonId = module?.lessons?.firstOrNull()?.id,
                        isLoading = false,
                    )
                }
            },
            onError = { e -> updateState { copy(isLoading = false, error = e.message) } },
        )
    }

    private fun toggleLesson(lessonId: String) {
        updateState {
            copy(expandedLessonId = if (expandedLessonId == lessonId) null else lessonId)
        }
    }
}
