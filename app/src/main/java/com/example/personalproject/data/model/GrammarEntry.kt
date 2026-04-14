package app.kotori.japanese.data.model

data class GrammarEntry(
    val id: String,
    val lessonNumber: Int,
    val title: String,
    val content: String,
    val exampleOne: String,
    val exampleTwo: String,
    val supportingContent: String,
    val jlptLevel: String,
    val category: String,
    val relatedGrammarReferences: List<String>,  // "|" delimited
    val relatedKanjiReferences: List<String>,    // "|" delimited
    val relatedVocabReferences: List<String>,    // "|" delimited
    val difficultyOrder: Int,
    val unlocksContent: String,
)
