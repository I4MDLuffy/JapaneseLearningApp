package app.kotori.japanese.verbs.detail.mvi

import app.kotori.japanese.data.repository.VerbRepository
import app.kotori.japanese.mvi.BaseViewModel

class VerbDetailViewModel(
    private val repository: VerbRepository,
    private val verbId: String,
) : BaseViewModel<VerbDetailState, VerbDetailAction>(VerbDetailState()) {

    init {
        dispatchAction(VerbDetailAction.Load)
    }

    override fun dispatchAction(action: VerbDetailAction) {
        when (action) {
            VerbDetailAction.Load -> load()
        }
    }

    private fun load() {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getVerbById(verbId) },
            onSuccess = { entry -> updateState { copy(entry = entry, isLoading = false) } },
            onError = { e -> updateState { copy(isLoading = false, error = e.message) } },
        )
    }
}
