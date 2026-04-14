package app.kotori.japanese.grammar.detail.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface GrammarDetailAction : BaseAction {
    data object Load : GrammarDetailAction
}
