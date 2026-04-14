package app.kotori.japanese.ui.games.mvi

/**
 * Unified study card for all content types (vocab, kanji, verb, adjective, noun, phrase).
 * Used by all vocabulary-style games so any content type can be studied in any game.
 */
data class StudyItem(
    val id: String,
    val type: String,       // "vocab" | "kanji" | "verb" | "adjective" | "noun" | "phrase"
    val question: String,   // Primary Japanese display (front of card)
    val reading: String,    // Hiragana reading
    val romaji: String,
    val answer: String,     // English meaning (back of card)
    val jlptLevel: String = "",
    val category: String? = null,
)
