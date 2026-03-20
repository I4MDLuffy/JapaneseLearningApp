package com.example.personalproject.learning.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface LevelAction : BaseAction {
    object Load : LevelAction
}
