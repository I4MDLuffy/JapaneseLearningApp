package app.kotori.japanese.grammar.detail.mvi

import app.kotori.japanese.data.model.GrammarEntry
import app.kotori.japanese.mvi.BaseState

data class GrammarDetailState(
    val entry: GrammarEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
