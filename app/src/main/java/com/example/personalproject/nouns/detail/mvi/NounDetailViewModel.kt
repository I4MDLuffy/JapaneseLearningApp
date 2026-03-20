package com.example.personalproject.nouns.detail.mvi

import com.example.personalproject.data.repository.NounRepository
import com.example.personalproject.mvi.BaseViewModel

class NounDetailViewModel(
    private val repository: NounRepository,
    private val nounId: String,
) : BaseViewModel<NounDetailState, NounDetailAction>(NounDetailState()) {

    init {
        dispatchAction(NounDetailAction.Load)
    }

    override fun dispatchAction(action: NounDetailAction) {
        when (action) {
            NounDetailAction.Load -> load()
        }
    }

    private fun load() {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getNounById(nounId) },
            onSuccess = { entry -> updateState { copy(entry = entry, isLoading = false) } },
            onError = { e -> updateState { copy(isLoading = false, error = e.message) } },
        )
    }
}
