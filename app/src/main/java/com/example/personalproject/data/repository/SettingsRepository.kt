package com.example.personalproject.data.repository

import android.content.Context
import com.example.personalproject.data.model.AppSettings
import com.example.personalproject.data.model.AppTheme
import com.example.personalproject.data.model.StudyDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("kotoba_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun load() = AppSettings(
        theme = AppTheme.entries.find { it.name == prefs.getString("theme", AppTheme.SAKURA.name) }
            ?: AppTheme.SAKURA,
        isDarkMode = prefs.getBoolean("dark_mode", false),
        masterVolume = prefs.getFloat("master_volume", 1.0f),
        showRomaji = prefs.getBoolean("show_romaji", true),
        showFurigana = prefs.getBoolean("show_furigana", true),
        studyDirection = StudyDirection.entries.find {
            it.name == prefs.getString("study_direction", StudyDirection.EN_TO_JP.name)
        } ?: StudyDirection.EN_TO_JP,
        structuredLearning = prefs.getBoolean("structured_learning", true),
        largerText = prefs.getBoolean("larger_text", false),
        highContrast = prefs.getBoolean("high_contrast", false),
    )

    private fun persist(block: () -> Unit) {
        block()
        _settings.value = load()
    }

    fun updateTheme(theme: AppTheme) =
        persist { prefs.edit().putString("theme", theme.name).apply() }

    fun updateDarkMode(dark: Boolean) =
        persist { prefs.edit().putBoolean("dark_mode", dark).apply() }

    fun updateVolume(volume: Float) =
        persist { prefs.edit().putFloat("master_volume", volume).apply() }

    fun updateShowRomaji(show: Boolean) =
        persist { prefs.edit().putBoolean("show_romaji", show).apply() }

    fun updateShowFurigana(show: Boolean) =
        persist { prefs.edit().putBoolean("show_furigana", show).apply() }

    fun updateStudyDirection(direction: StudyDirection) =
        persist { prefs.edit().putString("study_direction", direction.name).apply() }

    fun updateStructuredLearning(enabled: Boolean) =
        persist { prefs.edit().putBoolean("structured_learning", enabled).apply() }

    fun updateLargerText(enabled: Boolean) =
        persist { prefs.edit().putBoolean("larger_text", enabled).apply() }

    fun updateHighContrast(enabled: Boolean) =
        persist { prefs.edit().putBoolean("high_contrast", enabled).apply() }
}
