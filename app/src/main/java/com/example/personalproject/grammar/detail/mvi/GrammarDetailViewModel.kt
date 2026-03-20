package com.example.personalproject.grammar.detail.mvi

import com.example.personalproject.data.repository.GrammarRepository
import com.example.personalproject.mvi.BaseViewModel

class GrammarDetailViewModel(
    private val repository: GrammarRepository,
    private val grammarId: String,
) : BaseViewModel<GrammarDetailState, GrammarDetailAction>(GrammarDetailState()) {

    init {
        dispatchAction(GrammarDetailAction.Load)
    }

    override fun dispatchAction(action: GrammarDetailAction) {
        when (action) {
            GrammarDetailAction.Load -> load()
        }
    }

    private fun load() {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getGrammarById(grammarId) },
            onSuccess = { entry -> updateState { copy(entry = entry, isLoading = false) } },
            onError = { e -> updateState { copy(isLoading = false, error = e.message) } },
        )
    }
}
