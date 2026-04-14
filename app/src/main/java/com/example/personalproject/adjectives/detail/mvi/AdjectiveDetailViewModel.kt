package app.kotori.japanese.adjectives.detail.mvi

import app.kotori.japanese.data.repository.AdjectiveRepository
import app.kotori.japanese.mvi.BaseViewModel

class AdjectiveDetailViewModel(
    private val repository: AdjectiveRepository,
    private val adjId: String,
) : BaseViewModel<AdjectiveDetailState, AdjectiveDetailAction>(AdjectiveDetailState()) {

    init {
        dispatchAction(AdjectiveDetailAction.Load)
    }

    override fun dispatchAction(action: AdjectiveDetailAction) {
        when (action) {
            AdjectiveDetailAction.Load -> load()
        }
    }

    private fun load() {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getAdjectiveById(adjId) },
            onSuccess = { entry -> updateState { copy(entry = entry, isLoading = false) } },
            onError = { e -> updateState { copy(isLoading = false, error = e.message) } },
        )
    }
}
