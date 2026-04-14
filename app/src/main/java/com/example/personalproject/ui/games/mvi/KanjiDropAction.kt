package app.kotori.japanese.ui.games.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface KanjiDropAction : BaseAction {
    data class SetMode(val mode: KanjiDropMode) : KanjiDropAction
    data class SelectAnswer(val option: String) : KanjiDropAction
    data class DropTimerTick(val progress: Float) : KanjiDropAction
    object DropTimerExpired : KanjiDropAction
    object Restart : KanjiDropAction
}
