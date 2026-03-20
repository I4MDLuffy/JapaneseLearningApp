package com.example.personalproject.nouns.detail.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface NounDetailAction : BaseAction {
    data object Load : NounDetailAction
}
