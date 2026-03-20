package com.example.personalproject.verbs.detail.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface VerbDetailAction : BaseAction {
    data object Load : VerbDetailAction
}
