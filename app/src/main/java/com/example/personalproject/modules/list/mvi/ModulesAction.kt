package app.kotori.japanese.modules.list.mvi

import app.kotori.japanese.data.model.ModuleCategory
import app.kotori.japanese.mvi.BaseAction

sealed class ModulesAction : BaseAction {
    data object LoadModules : ModulesAction()
    data class FilterByCategory(val category: ModuleCategory?) : ModulesAction()
}
