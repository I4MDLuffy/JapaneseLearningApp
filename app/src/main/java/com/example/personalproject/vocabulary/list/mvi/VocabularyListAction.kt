package app.kotori.japanese.vocabulary.list.mvi

import app.kotori.japanese.mvi.BaseAction

sealed class VocabularyListAction : BaseAction {
    data object LoadWords : VocabularyListAction()
    data class Search(val query: String) : VocabularyListAction()
    data class FilterByJlpt(val level: String?) : VocabularyListAction()
    data class FilterByPartOfSpeech(val pos: String?) : VocabularyListAction()
    data object ClearFilters : VocabularyListAction()
}
