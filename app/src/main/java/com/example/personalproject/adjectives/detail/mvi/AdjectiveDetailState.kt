package app.kotori.japanese.adjectives.detail.mvi

import app.kotori.japanese.data.model.AdjectiveEntry
import app.kotori.japanese.mvi.BaseState

data class AdjectiveDetailState(
    val entry: AdjectiveEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
