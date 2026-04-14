package app.kotori.japanese.grammar.list.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface GrammarListAction : BaseAction {
    data object LoadEntries : GrammarListAction
    data class Search(val query: String) : GrammarListAction
    data class FilterByJlpt(val level: String?) : GrammarListAction
    data object ClearFilters : GrammarListAction
}
