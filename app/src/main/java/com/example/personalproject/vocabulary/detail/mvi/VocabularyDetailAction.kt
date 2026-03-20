package com.example.personalproject.vocabulary.detail.mvi

import com.example.personalproject.mvi.BaseAction

sealed class VocabularyDetailAction : BaseAction {
    data class LoadWord(val id: String) : VocabularyDetailAction()
}
