package app.kotori.japanese.radicals.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface RadicalGameAction : BaseAction {
    object FlipCard : RadicalGameAction
    object NextCard : RadicalGameAction
    data class SelectOption(val option: String) : RadicalGameAction
    object SwitchToFlashcard : RadicalGameAction
    object SwitchToMultipleChoice : RadicalGameAction
    object Restart : RadicalGameAction
}
