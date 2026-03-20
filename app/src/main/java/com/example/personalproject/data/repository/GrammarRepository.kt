package com.example.personalproject.data.repository

import android.content.Context
import com.example.personalproject.data.csv.CsvParser
import com.example.personalproject.data.model.GrammarEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GrammarRepository(private val context: Context) {

    private var cache: List<GrammarEntry>? = null

    suspend fun getAllGrammar(): List<GrammarEntry> =
        cache ?: load().also { cache = it }

    suspend fun getGrammarById(id: String): GrammarEntry? =
        getAllGrammar().find { it.id == id }

    suspend fun filterByCategory(category: String): List<GrammarEntry> =
        getAllGrammar().filter { it.category.equals(category, ignoreCase = true) }

    suspend fun filterByJlpt(level: String): List<GrammarEntry> =
        getAllGrammar().filter { it.jlptLevel == level }

    suspend fun search(query: String): List<GrammarEntry> {
        if (query.isBlank()) return getAllGrammar()
        val q = query.trim()
        return getAllGrammar().filter { g ->
            g.title.contains(q, ignoreCase = true) ||
                g.content.contains(q, ignoreCase = true) ||
                g.category.contains(q, ignoreCase = true)
        }
    }

    private suspend fun load(): List<GrammarEntry> =
        withContext(Dispatchers.IO) { CsvParser.parseGrammar(context, "grammar.csv") }
}
