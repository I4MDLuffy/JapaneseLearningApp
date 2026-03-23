package com.example.personalproject.ui.games.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface KanjiBuilderAction : BaseAction {
    data class TapTile(val radicalId: String) : KanjiBuilderAction
    object Restart : KanjiBuilderAction
}
