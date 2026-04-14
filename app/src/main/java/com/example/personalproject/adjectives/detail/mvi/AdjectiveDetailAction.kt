package app.kotori.japanese.adjectives.detail.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface AdjectiveDetailAction : BaseAction {
    data object Load : AdjectiveDetailAction
}
