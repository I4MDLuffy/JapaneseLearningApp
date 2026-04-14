package app.kotori.japanese.ui.games.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface KanjiBuilderAction : BaseAction {
    data class TapTile(val radicalId: String) : KanjiBuilderAction
    object Restart : KanjiBuilderAction
}
