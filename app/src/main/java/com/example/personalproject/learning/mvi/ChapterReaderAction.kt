package com.example.personalproject.learning.mvi

import com.example.personalproject.mvi.BaseAction

sealed interface ChapterReaderAction : BaseAction {
    object NextItem : ChapterReaderAction
    object RevealStudyVocab : ChapterReaderAction
    object CompleteChapter : ChapterReaderAction
}
