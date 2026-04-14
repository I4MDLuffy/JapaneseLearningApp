package app.kotori.japanese.ui.basiccharacters

import app.kotori.japanese.mvi.BaseAction

sealed interface KanaGameAction : BaseAction {
    // Mode select
    data class SelectMode(val mode: GameMode) : KanaGameAction
    object Restart : KanaGameAction

    // Matching
    data class TapMatchCard(val index: Int) : KanaGameAction

    // Typing
    data class UpdateTypingInput(val text: String) : KanaGameAction
    object SubmitTyping : KanaGameAction

    // Flashcard
    object FlipFlashcard : KanaGameAction
    data class AnswerFlashcard(val correct: Boolean) : KanaGameAction

    // Multiple choice
    data class SelectChoice(val choice: String) : KanaGameAction

    // Kana speed
    data class UpdateSpeedInput(val text: String) : KanaGameAction
    object SubmitSpeed : KanaGameAction
    data class SpeedTimerUpdate(val fraction: Float) : KanaGameAction
    object SpeedTimerExpired : KanaGameAction
}
