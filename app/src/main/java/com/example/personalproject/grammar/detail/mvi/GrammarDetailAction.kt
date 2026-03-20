package com.example.personalproject.grammar.detail.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface GrammarDetailAction : BaseAction {
    data object Load : GrammarDetailAction
}
