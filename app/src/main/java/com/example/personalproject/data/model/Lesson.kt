package app.kotori.japanese.data.model

/**
 * A single lesson inside a [LearningModule].
 *
 * Content is a list of [LessonSection]s so you can mix explanations,
 * grammar rules, and vocabulary items in any order within one lesson.
 */
data class Lesson(
    val id: String,
    val title: String,
    val sections: List<LessonSection>,
)

sealed class LessonSection {
    /** Plain explanatory text. Supports basic newline formatting. */
    data class Text(val body: String) : LessonSection()

    /** A grammar pattern with structured examples. */
    data class GrammarRule(
        val pattern: String,
        val description: String,
        val examples: List<GrammarExample>,
    ) : LessonSection()

    /** One or more vocabulary words embedded in a lesson. */
    data class VocabList(val words: List<VocabularyWord>) : LessonSection()
}

data class GrammarExample(
    val japanese: String,
    val romaji: String,
    val english: String,
)
