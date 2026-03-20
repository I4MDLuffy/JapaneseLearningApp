package com.example.personalproject.data.repository

import android.content.Context

class ChapterProgressRepository(context: Context) {

    private val prefs = context.getSharedPreferences("kotoba_chapter_progress", Context.MODE_PRIVATE)

    fun isCompleted(level: String, chapterIndex: Int): Boolean =
        prefs.getBoolean("${level}_$chapterIndex", false)

    fun markCompleted(level: String, chapterIndex: Int) {
        prefs.edit().putBoolean("${level}_$chapterIndex", true).apply()
    }
}
