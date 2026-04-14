package app.kotori.japanese.nouns.detail.mvi

import app.kotori.japanese.data.model.NounEntry
import app.kotori.japanese.mvi.BaseState

data class NounDetailState(
    val entry: NounEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
