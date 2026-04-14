package app.kotori.japanese.adjectives.list.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface AdjectiveListAction : BaseAction {
    data object LoadEntries : AdjectiveListAction
    data class Search(val query: String) : AdjectiveListAction
    data class FilterByJlpt(val level: String?) : AdjectiveListAction
    data object ClearFilters : AdjectiveListAction
}
