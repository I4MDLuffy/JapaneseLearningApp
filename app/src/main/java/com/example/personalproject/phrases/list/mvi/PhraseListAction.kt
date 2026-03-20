package com.example.personalproject.phrases.list.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface PhraseListAction : BaseAction {
    data object LoadEntries : PhraseListAction
    data class Search(val query: String) : PhraseListAction
    data class FilterByJlpt(val level: String?) : PhraseListAction
    data object ClearFilters : PhraseListAction
}
