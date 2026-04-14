package app.kotori.japanese.data.model

enum class AppTheme(val displayName: String) {
    JADE("Jade"),
    SORBET("Sorbet"),
    SAPPHIRE("Sapphire"),
    AMETHYST("Amethyst"),
    SAKURA("Sakura"),
}

enum class StudyDirection(val displayName: String) {
    EN_TO_JP("English → Japanese"),
    JP_TO_EN("Japanese → English"),
}

data class AppSettings(
    val theme: AppTheme = AppTheme.SAKURA,
    val isDarkMode: Boolean = false,
    val masterVolume: Float = 1.0f,
    val showRomaji: Boolean = true,
    val showFurigana: Boolean = true,
    val studyDirection: StudyDirection = StudyDirection.EN_TO_JP,
    // Structured learning
    val structuredLearning: Boolean = true,
    // Accessibility
    val largerText: Boolean = false,
    val highContrast: Boolean = false,
)
