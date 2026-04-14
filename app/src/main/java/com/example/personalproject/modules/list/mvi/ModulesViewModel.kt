package app.kotori.japanese.modules.list.mvi

import app.kotori.japanese.data.model.ModuleCategory
import app.kotori.japanese.data.repository.ModuleRepository
import app.kotori.japanese.mvi.BaseViewModel

class ModulesViewModel(
    private val repository: ModuleRepository,
) : BaseViewModel<ModulesState, ModulesAction>(ModulesState()) {

    init {
        dispatchAction(ModulesAction.LoadModules)
    }

    override fun dispatchAction(action: ModulesAction) {
        when (action) {
            ModulesAction.LoadModules -> loadModules()
            is ModulesAction.FilterByCategory -> filterByCategory(action.category)
        }
    }

    private fun loadModules() {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getAllModules() },
            onSuccess = { modules ->
                updateState {
                    copy(
                        allModules = modules,
                        displayedModules = modules,
                        isLoading = false,
                    )
                }
            },
            onError = { updateState { copy(isLoading = false) } },
        )
    }

    private fun filterByCategory(category: ModuleCategory?) {
        val filtered = if (category == null) {
            uiState.value.allModules
        } else {
            uiState.value.allModules.filter { it.category == category }
        }
        updateState { copy(selectedCategory = category, displayedModules = filtered) }
    }
}
