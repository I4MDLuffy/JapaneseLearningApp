package com.example.personalproject.modules.list.mvi

import com.example.personalproject.data.model.ModuleCategory
import com.example.personalproject.mvi.BaseAction

sealed class ModulesAction : BaseAction {
    data object LoadModules : ModulesAction()
    data class FilterByCategory(val category: ModuleCategory?) : ModulesAction()
}
