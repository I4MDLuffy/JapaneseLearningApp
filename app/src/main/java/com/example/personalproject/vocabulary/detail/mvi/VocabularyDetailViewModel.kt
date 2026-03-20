package com.example.personalproject.vocabulary.detail.mvi

import com.example.personalproject.data.repository.VocabularyRepository
import com.example.personalproject.mvi.BaseViewModel

class VocabularyDetailViewModel(
    private val repository: VocabularyRepository,
    private val wordId: String,
) : BaseViewModel<VocabularyDetailState, VocabularyDetailAction>(VocabularyDetailState()) {

    init {
        dispatchAction(VocabularyDetailAction.LoadWord(wordId))
    }

    override fun dispatchAction(action: VocabularyDetailAction) {
        when (action) {
            is VocabularyDetailAction.LoadWord -> loadWord(action.id)
        }
    }

    private fun loadWord(id: String) {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getWordById(id) },
            onSuccess = { word -> updateState { copy(word = word, isLoading = false) } },
            onError = { e -> updateState { copy(isLoading = false, error = e.message) } },
        )
    }
}
