package com.example.personalproject.adjectives.list.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface AdjectiveListAction : BaseAction {
    data object LoadEntries : AdjectiveListAction
    data class Search(val query: String) : AdjectiveListAction
    data class FilterByJlpt(val level: String?) : AdjectiveListAction
    data object ClearFilters : AdjectiveListAction
}
