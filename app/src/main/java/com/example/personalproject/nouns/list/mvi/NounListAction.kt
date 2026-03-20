package com.example.personalproject.nouns.list.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface NounListAction : BaseAction {
    data object LoadEntries : NounListAction
    data class Search(val query: String) : NounListAction
    data class FilterByJlpt(val level: String?) : NounListAction
    data object ClearFilters : NounListAction
}
