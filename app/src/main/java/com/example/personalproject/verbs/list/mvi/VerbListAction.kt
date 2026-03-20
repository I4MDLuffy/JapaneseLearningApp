package com.example.personalproject.verbs.list.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface VerbListAction : BaseAction {
    data object LoadEntries : VerbListAction
    data class Search(val query: String) : VerbListAction
    data class FilterByJlpt(val level: String?) : VerbListAction
    data object ClearFilters : VerbListAction
}
