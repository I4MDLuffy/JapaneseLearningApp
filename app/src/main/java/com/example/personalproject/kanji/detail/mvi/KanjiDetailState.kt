package app.kotori.japanese.kanji.detail.mvi

import app.kotori.japanese.data.model.KanjiEntry
import app.kotori.japanese.mvi.BaseState

data class KanjiDetailState(
    val entry: KanjiEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
