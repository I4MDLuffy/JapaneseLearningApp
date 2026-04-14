package app.kotori.japanese.data.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

class ChapterProgressRepository(private val prefs: Settings) {

    fun isCompleted(level: String, chapterIndex: Int): Boolean =
        prefs["${level}_$chapterIndex", false]

    fun markCompleted(level: String, chapterIndex: Int) {
        prefs["${level}_$chapterIndex"] = true
    }
}
