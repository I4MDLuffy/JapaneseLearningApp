package app.kotori.japanese.vocabulary.detail.mvi

import app.kotori.japanese.mvi.BaseAction

sealed class VocabularyDetailAction : BaseAction {
    data class LoadWord(val id: String) : VocabularyDetailAction()
}
