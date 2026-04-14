package app.kotori.japanese.data.model

enum class ChapterType { GRAMMAR, KANJI, VOCAB, STUDY_VOCAB, TERM_STUDY }

data class Chapter(
    val index: Int,
    val type: ChapterType,
    val setIndex: Int,
    val title: String,
    val isCompleted: Boolean,
    val isUnlocked: Boolean,
)
