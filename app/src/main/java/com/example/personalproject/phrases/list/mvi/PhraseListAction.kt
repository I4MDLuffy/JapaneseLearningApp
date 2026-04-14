package app.kotori.japanese.phrases.list.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface PhraseListAction : BaseAction {
    data object LoadEntries : PhraseListAction
    data class Search(val query: String) : PhraseListAction
    data class FilterByJlpt(val level: String?) : PhraseListAction
    data object ClearFilters : PhraseListAction
}
