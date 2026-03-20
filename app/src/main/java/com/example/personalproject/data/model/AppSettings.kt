package com.example.personalproject.data.model

enum class AppTheme(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark"),
    SAKURA("Sakura"),       // soft pink / gold
    OCEAN("Ocean"),         // teal / deep blue
    FOREST("Forest"),       // dark green / amber
}

enum class StudyDirection(val displayName: String) {
    EN_TO_JP("English → Japanese"),
    JP_TO_EN("Japanese → English"),
}

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
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
