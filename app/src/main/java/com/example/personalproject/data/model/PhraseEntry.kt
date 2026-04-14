package app.kotori.japanese.data.model

data class PhraseEntry(
    val id: String,
    val phrase: String,
    val reading: String,
    val meaning: String,
    val vocabularyReferences: List<String>,  // "|" delimited vocab ids
    val kanjiReferences: List<String>,       // "|" delimited kanji ids
    val jlptLevel: String,
    val category: String,
    val romaji: String,
    val grammarReferences: List<String>,     // "|" delimited grammar ids
)
