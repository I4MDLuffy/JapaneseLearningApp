package com.example.personalproject.learning.mvi

import com.example.personalproject.data.model.GrammarEntry
import com.example.personalproject.data.model.VocabularyWord
import com.example.personalproject.mvi.BaseState

sealed interface ChapterItem {
    data class GrammarItem(val entry: GrammarEntry) : ChapterItem
    data class VocabItem(val word: VocabularyWord) : ChapterItem
    data class StudyVocabItem(val word: VocabularyWord) : ChapterItem
}

data class ChapterReaderState(
    val chapterTitle: String = "",
    val items: List<ChapterItem> = emptyList(),
    val currentIndex: Int = 0,
    val isRevealed: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isCompleted: Boolean = false,
) : BaseState
