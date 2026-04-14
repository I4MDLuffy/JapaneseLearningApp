package app.kotori.japanese.data.model

data class NounEntry(
    val id: String,
    val kanji: String,
    val kanjiRadicals: List<String>,       // "|" delimited
    val romaji: String,
    val pitchAccent: String,
    val hiragana: String,
    val exampleReferences: List<String>,   // "|" delimited phrase ids
    val alternateReading: String,
    val meaning: String,
    val theme: String,
    val radicalReferences: List<String>,   // "|" delimited radical ids
    val jlptLevel: String,
    val unlockedAtGrammarId: String,
)
