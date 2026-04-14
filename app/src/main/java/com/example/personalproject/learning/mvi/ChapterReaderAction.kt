package com.example.personalproject.learning.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface ChapterReaderAction : BaseAction {
    object NextItem : ChapterReaderAction
    object PreviousItem : ChapterReaderAction
    object RevealStudyVocab : ChapterReaderAction
    object ReviewAgain : ChapterReaderAction
    object CompleteChapter : ChapterReaderAction
    data class SelectMcOption(val option: String) : ChapterReaderAction
    object ToggleStudyMode : ChapterReaderAction
}
