package com.example.personalproject.data.repository

import android.content.Context
import com.example.personalproject.data.csv.CsvParser
import com.example.personalproject.data.model.PhraseEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhraseRepository(private val context: Context) {

    private var cache: List<PhraseEntry>? = null

    suspend fun getAllPhrases(): List<PhraseEntry> =
        cache ?: load().also { cache = it }

    suspend fun getPhraseById(id: String): PhraseEntry? =
        getAllPhrases().find { it.id == id }

    suspend fun filterByCategory(category: String): List<PhraseEntry> =
        getAllPhrases().filter { it.category.equals(category, ignoreCase = true) }

    suspend fun filterByJlpt(level: String): List<PhraseEntry> =
        getAllPhrases().filter { it.jlptLevel == level }

    suspend fun search(query: String): List<PhraseEntry> {
        if (query.isBlank()) return getAllPhrases()
        val q = query.trim()
        return getAllPhrases().filter { p ->
            p.phrase.contains(q) ||
                p.reading.contains(q) ||
                p.meaning.contains(q, ignoreCase = true)
        }
    }

    private suspend fun load(): List<PhraseEntry> =
        withContext(Dispatchers.IO) { CsvParser.parsePhrases(context, "phrases.csv") }
}
