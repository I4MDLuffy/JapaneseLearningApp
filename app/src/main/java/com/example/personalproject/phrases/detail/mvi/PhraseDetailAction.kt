package com.example.personalproject.phrases.detail.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface PhraseDetailAction : BaseAction {
    data object Load : PhraseDetailAction
}
