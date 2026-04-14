package app.kotori.japanese.verbs.detail.mvi

import app.kotori.japanese.data.model.VerbEntry
import app.kotori.japanese.mvi.BaseState

data class VerbDetailState(
    val entry: VerbEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
