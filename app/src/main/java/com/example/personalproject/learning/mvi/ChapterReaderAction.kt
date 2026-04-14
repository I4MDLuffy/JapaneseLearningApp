package app.kotori.japanese.learning.mvi

import app.kotori.japanese.mvi.BaseAction

sealed interface ChapterReaderAction : BaseAction {
    object NextItem : ChapterReaderAction
    object PreviousItem : ChapterReaderAction
    object RevealStudyVocab : ChapterReaderAction
    object ReviewAgain : ChapterReaderAction
    object CompleteChapter : ChapterReaderAction
    data class SelectMcOption(val option: String) : ChapterReaderAction
    object ToggleStudyMode : ChapterReaderAction
}
