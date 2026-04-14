package app.kotori.japanese.verbs.detail.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface VerbDetailAction : BaseAction {
    data object Load : VerbDetailAction
}
