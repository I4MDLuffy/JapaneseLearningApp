package app.kotori.japanese.data.model

data class VocabularyWord(
    val id: String,
    val japanese: String,
    val hiragana: String,
    val kanjiReferences: List<String>,      // "|" delimited kanji ids
    val romaji: String,
    val english: String,
    val category: String,
    val jlptLevel: String,
    val exampleJapanese: String,
    val exampleEnglish: String,
    val notes: String,
    val partOfSpeech: String,
    val partOfSpeechReferences: List<String>, // "|" delimited
    val frequency: String,
    val pitchAccent: String,
)
