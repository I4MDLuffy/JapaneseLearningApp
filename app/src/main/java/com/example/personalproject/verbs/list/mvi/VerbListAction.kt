package app.kotori.japanese.verbs.list.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface VerbListAction : BaseAction {
    data object LoadEntries : VerbListAction
    data class Search(val query: String) : VerbListAction
    data class FilterByJlpt(val level: String?) : VerbListAction
    data object ClearFilters : VerbListAction
}
