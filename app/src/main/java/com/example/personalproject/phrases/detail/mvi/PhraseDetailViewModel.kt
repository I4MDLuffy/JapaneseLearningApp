package app.kotori.japanese.phrases.detail.mvi

import app.kotori.japanese.data.repository.PhraseRepository
import app.kotori.japanese.mvi.BaseViewModel

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
