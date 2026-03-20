package com.example.personalproject.data.repository

import android.content.Context
import com.example.personalproject.data.csv.CsvParser
import com.example.personalproject.data.model.AdjectiveEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdjectiveRepository(private val context: Context) {

    private var cache: List<AdjectiveEntry>? = null

    suspend fun getAllAdjectives(): List<AdjectiveEntry> =
        cache ?: load().also { cache = it }

    suspend fun getAdjectiveById(id: String): AdjectiveEntry? =
        getAllAdjectives().find { it.id == id }

    suspend fun filterByJlpt(level: String): List<AdjectiveEntry> =
        getAllAdjectives().filter { it.jlptLevel == level }

    suspend fun filterByType(adjType: String): List<AdjectiveEntry> =
        getAllAdjectives().filter { it.adjType.equals(adjType, ignoreCase = true) }

    suspend fun search(query: String): List<AdjectiveEntry> {
        if (query.isBlank()) return getAllAdjectives()
        val q = query.trim()
        return getAllAdjectives().filter { a ->
            a.kanji.contains(q) ||
                a.hiragana.contains(q) ||
                a.romaji.contains(q, ignoreCase = true) ||
                a.meaning.contains(q, ignoreCase = true)
        }
    }

    private suspend fun load(): List<AdjectiveEntry> =
        withContext(Dispatchers.IO) { CsvParser.parseAdjectives(context, "adjectives.csv") }
}
