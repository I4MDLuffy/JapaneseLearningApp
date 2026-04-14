package app.kotori.japanese.nouns.detail.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface NounDetailAction : BaseAction {
    data object Load : NounDetailAction
}
