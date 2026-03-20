package com.example.personalproject.phrases.detail.mvi

import com.example.personalproject.data.repository.PhraseRepository
import com.example.personalproject.mvi.BaseViewModel

class PhraseDetailViewModel(
    private val repository: PhraseRepository,
    private val phraseId: String,
) : BaseViewModel<PhraseDetailState, PhraseDetailAction>(PhraseDetailState()) {

    init {
        dispatchAction(PhraseDetailAction.Load)
    }

    override fun dispatchAction(action: PhraseDetailAction) {
        when (action) {
            PhraseDetailAction.Load -> load()
        }
    }

    private fun load() {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getPhraseById(phraseId) },
            onSuccess = { entry -> updateState { copy(entry = entry, isLoading = false) } },
            onError = { e -> updateState { copy(isLoading = false, error = e.message) } },
        )
    }
}
