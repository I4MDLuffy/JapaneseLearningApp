package app.kotori.japanese.learning.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface LevelAction : BaseAction {
    object Load : LevelAction
}
