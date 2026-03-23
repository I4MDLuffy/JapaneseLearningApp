package com.example.personalproject.ui.games.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface KanjiDropAction : BaseAction {
    data class SetMode(val mode: KanjiDropMode) : KanjiDropAction
    data class SelectAnswer(val option: String) : KanjiDropAction
    data class DropTimerTick(val progress: Float) : KanjiDropAction
    object DropTimerExpired : KanjiDropAction
    object Restart : KanjiDropAction
}
