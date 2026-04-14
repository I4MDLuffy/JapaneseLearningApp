package app.kotori.japanese.phrases.detail.mvi

import app.kotori.japanese.data.model.PhraseEntry
import app.kotori.japanese.mvi.BaseState

data class PhraseDetailState(
    val entry: PhraseEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
