package com.example.personalproject.data.model

enum class ChapterType { GRAMMAR, VOCAB, STUDY_VOCAB, TERM_STUDY }

data class Chapter(
    val index: Int,
    val type: ChapterType,
    val setIndex: Int,
    val title: String,
    val isCompleted: Boolean,
    val isUnlocked: Boolean,
)
