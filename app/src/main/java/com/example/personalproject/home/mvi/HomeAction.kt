package com.example.personalproject.home.mvi

import com.example.personalproject.mvi.BaseAction

sealed class HomeAction : BaseAction {
    data object LoadData : HomeAction()
    data object RefreshWordOfTheDay : HomeAction()
}
