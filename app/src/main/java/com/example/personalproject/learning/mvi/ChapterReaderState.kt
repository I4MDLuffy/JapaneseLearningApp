package com.example.personalproject.learning.mvi

import com.example.personalproject.data.model.GrammarEntry
import com.example.personalproject.data.model.KanjiEntry
import com.example.personalproject.data.model.VocabularyWord
import com.example.personalproject.mvi.BaseState

sealed interface ChapterItem {
    data class GrammarItem(val entry: GrammarEntry) : ChapterItem
    data class VocabItem(val word: VocabularyWord) : ChapterItem
    data class StudyVocabItem(val word: VocabularyWord) : ChapterItem

    /** A dedicated kanji study card shown in KANJI chapters. */
    data class KanjiItem(val entry: KanjiEntry) : ChapterItem

    /** A study card for any term type (vocab, verb, adjective, noun, phrase, kanji). */
    data class TermStudyItem(
        val id: String,
        val type: String,          // "vocab" | "verb" | "adjective" | "noun" | "phrase" | "kanji"
        val displayScript: String, // Japanese/kanji shown on the front
        val reading: String,       // hiragana reading
        val romaji: String,
        val meaning: String,
    ) : ChapterItem
}

enum class StudyCardMode { FLASHCARD, MULTIPLE_CHOICE }

data class ChapterReaderState(
    val chapterTitle: String = "",
    val items: List<ChapterItem> = emptyList(),
    val currentIndex: Int = 0,
    val isRevealed: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isCompleted: Boolean = false,
    // Null when this is the last chapter in the level
    val nextChapterType: String? = null,
    val nextSetIndex: Int? = null,
    val nextChapterTitle: String? = null,
    // Multiple Choice mode for study cards
    val studyCardMode: StudyCardMode = StudyCardMode.MULTIPLE_CHOICE,
    val mcOptions: List<String> = emptyList(),
    val selectedMcOption: String? = null,
    val mcIsCorrect: Boolean? = null,
) : BaseState
