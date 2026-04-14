package com.example.personalproject.ui.games.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface StudyGameAction : BaseAction {
    object Restart : StudyGameAction

    // Flashcard
    object FlipCard : StudyGameAction
    object GotIt : StudyGameAction
    object ReviewAgain : StudyGameAction

    // Timed Quiz
    data class SelectOption(val option: String) : StudyGameAction
    data class QuizTimerUpdate(val fraction: Float) : StudyGameAction
    object QuizTimerExpired : StudyGameAction

    // Match Pairs
    data class TapCard(val index: Int) : StudyGameAction
    data class SetPairMode(val mode: PairMode) : StudyGameAction

    // Kana Speed (grid-tap — each tap validated immediately)
    data class TapGridTile(val index: Int) : StudyGameAction
    data class SpeedTimerUpdate(val fraction: Float) : StudyGameAction
    object SpeedTimerExpired : StudyGameAction

    // Kana Swipe (tap-to-path, no timer)
    data class TapSwipeTile(val index: Int) : StudyGameAction
    object UndoSwipe : StudyGameAction
    object SubmitSwipe : StudyGameAction

    // Fill in the Blank
    data class SetFillBlankDirection(val direction: FillBlankDirection) : StudyGameAction
    data class UpdateFillBlankInput(val text: String) : StudyGameAction
    object SubmitFillBlank : StudyGameAction
    object NextFillBlank : StudyGameAction
}
