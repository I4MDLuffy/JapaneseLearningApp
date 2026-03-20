package com.example.personalproject.vocabulary.list.mvi

import com.example.personalproject.mvi.BaseAction

sealed class VocabularyListAction : BaseAction {
    data object LoadWords : VocabularyListAction()
    data class Search(val query: String) : VocabularyListAction()
    data class FilterByJlpt(val level: String?) : VocabularyListAction()
    data class FilterByPartOfSpeech(val pos: String?) : VocabularyListAction()
    data object ClearFilters : VocabularyListAction()
}
