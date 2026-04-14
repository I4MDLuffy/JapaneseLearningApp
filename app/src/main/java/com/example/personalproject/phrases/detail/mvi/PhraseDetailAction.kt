package app.kotori.japanese.phrases.detail.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface PhraseDetailAction : BaseAction {
    data object Load : PhraseDetailAction
}
