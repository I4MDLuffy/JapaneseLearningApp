package app.kotori.japanese.data.model

/**
 * A top-level learning module grouping related [Lesson]s under a [ModuleCategory].
 *
 * Add new modules by appending entries in [ModuleRepository].
 */
data class LearningModule(
    val id: String,
    val title: String,
    val description: String,
    val category: ModuleCategory,
    val iconEmoji: String,
    val lessons: List<Lesson>,
    val isUnlocked: Boolean = true,
)
