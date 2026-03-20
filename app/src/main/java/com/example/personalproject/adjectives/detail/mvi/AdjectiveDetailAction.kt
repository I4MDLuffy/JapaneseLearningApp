package com.example.personalproject.adjectives.detail.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface AdjectiveDetailAction : BaseAction {
    data object Load : AdjectiveDetailAction
}
