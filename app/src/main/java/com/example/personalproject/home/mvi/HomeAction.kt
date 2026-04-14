package app.kotori.japanese.home.mvi

import app.kotori.japanese.mvi.BaseAction

sealed class HomeAction : BaseAction {
    data object LoadData : HomeAction()
    data object RefreshWordOfTheDay : HomeAction()
}
