package app.kotori.japanese.kanji.detail.mvi

import app.kotori.japanese.data.repository.KanjiRepository
import app.kotori.japanese.mvi.BaseViewModel

class KanjiDetailViewModel(
    private val repository: KanjiRepository,
    private val kanjiId: String,
) : BaseViewModel<KanjiDetailState, KanjiDetailAction>(KanjiDetailState()) {

    init {
        dispatchAction(KanjiDetailAction.Load)
    }

    override fun dispatchAction(action: KanjiDetailAction) {
        when (action) {
            KanjiDetailAction.Load -> load()
        }
    }

    private fun load() {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getKanjiById(kanjiId) },
            onSuccess = { entry -> updateState { copy(entry = entry, isLoading = false) } },
            onError = { e -> updateState { copy(isLoading = false, error = e.message) } },
        )
    }
}
