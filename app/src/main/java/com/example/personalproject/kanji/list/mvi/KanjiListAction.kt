package com.example.personalproject.kanji.list.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface KanjiListAction : BaseAction {
    data object LoadEntries : KanjiListAction
    data class Search(val query: String) : KanjiListAction
    data class FilterByJlpt(val level: String?) : KanjiListAction
    data object ClearFilters : KanjiListAction
}
