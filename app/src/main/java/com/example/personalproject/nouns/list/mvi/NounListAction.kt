package app.kotori.japanese.nouns.list.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface NounListAction : BaseAction {
    data object LoadEntries : NounListAction
    data class Search(val query: String) : NounListAction
    data class FilterByJlpt(val level: String?) : NounListAction
    data object ClearFilters : NounListAction
}
