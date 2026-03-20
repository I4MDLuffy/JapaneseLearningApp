package com.example.personalproject.data.repository

import android.content.Context
import com.example.personalproject.data.csv.CsvParser
import com.example.personalproject.data.model.VerbEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VerbRepository(private val context: Context) {

    private var cache: List<VerbEntry>? = null

    suspend fun getAllVerbs(): List<VerbEntry> =
        cache ?: load().also { cache = it }

    suspend fun getVerbById(id: String): VerbEntry? =
        getAllVerbs().find { it.id == id }

    suspend fun filterByJlpt(level: String): List<VerbEntry> =
        getAllVerbs().filter { it.jlptLevel == level }

    suspend fun filterByType(verbType: String): List<VerbEntry> =
        getAllVerbs().filter { it.verbType.equals(verbType, ignoreCase = true) }

    suspend fun search(query: String): List<VerbEntry> {
        if (query.isBlank()) return getAllVerbs()
        val q = query.trim()
        return getAllVerbs().filter { v ->
            v.kanji.contains(q) ||
                v.dictionaryForm.contains(q) ||
                v.romaji.contains(q, ignoreCase = true) ||
                v.meaning.contains(q, ignoreCase = true)
        }
    }

    private suspend fun load(): List<VerbEntry> =
        withContext(Dispatchers.IO) { CsvParser.parseVerbs(context, "verbs.csv") }
}
