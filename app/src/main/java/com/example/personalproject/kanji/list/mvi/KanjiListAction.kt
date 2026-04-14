package app.kotori.japanese.kanji.list.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface KanjiListAction : BaseAction {
    data object LoadEntries : KanjiListAction
    data class Search(val query: String) : KanjiListAction
    data class FilterByJlpt(val level: String?) : KanjiListAction
    data object ClearFilters : KanjiListAction
}
