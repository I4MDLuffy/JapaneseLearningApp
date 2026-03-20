package com.example.personalproject.kanji.detail.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface KanjiDetailAction : BaseAction {
    data object Load : KanjiDetailAction
}
