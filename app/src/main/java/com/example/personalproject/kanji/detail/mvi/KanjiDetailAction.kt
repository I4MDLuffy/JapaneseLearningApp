package app.kotori.japanese.kanji.detail.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface KanjiDetailAction : BaseAction {
    data object Load : KanjiDetailAction
}
