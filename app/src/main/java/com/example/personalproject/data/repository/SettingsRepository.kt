package app.kotori.japanese.data.repository

import app.kotori.japanese.data.model.AppSettings
import app.kotori.japanese.data.model.AppTheme
import app.kotori.japanese.data.model.StudyDirection
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(private val prefs: Settings) {

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun load() = AppSettings(
        theme = AppTheme.entries.find { it.name == prefs.getStringOrNull("theme") }
            ?: AppTheme.SAKURA,
        isDarkMode = prefs["dark_mode", false],
        masterVolume = prefs["master_volume", 1.0f],
        showRomaji = prefs["show_romaji", true],
        showFurigana = prefs["show_furigana", true],
        studyDirection = StudyDirection.entries.find {
            it.name == prefs.getStringOrNull("study_direction")
        } ?: StudyDirection.EN_TO_JP,
        structuredLearning = prefs["structured_learning", true],
        largerText = prefs["larger_text", false],
        highContrast = prefs["high_contrast", false],
    )

    private fun persist(block: () -> Unit) {
        block()
        _settings.value = load()
    }

    fun updateTheme(theme: AppTheme) =
        persist { prefs["theme"] = theme.name }

    fun updateDarkMode(dark: Boolean) =
        persist { prefs["dark_mode"] = dark }

    fun updateVolume(volume: Float) =
        persist { prefs["master_volume"] = volume }

    fun updateShowRomaji(show: Boolean) =
        persist { prefs["show_romaji"] = show }

    fun updateShowFurigana(show: Boolean) =
        persist { prefs["show_furigana"] = show }

    fun updateStudyDirection(direction: StudyDirection) =
        persist { prefs["study_direction"] = direction.name }

    fun updateStructuredLearning(enabled: Boolean) =
        persist { prefs["structured_learning"] = enabled }

    fun updateLargerText(enabled: Boolean) =
        persist { prefs["larger_text"] = enabled }

    fun updateHighContrast(enabled: Boolean) =
        persist { prefs["high_contrast"] = enabled }
}
