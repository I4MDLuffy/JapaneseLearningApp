package com.example.personalproject.data.repository

import android.content.Context
import com.example.personalproject.data.csv.CsvParser
import com.example.personalproject.data.model.RadicalEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RadicalRepository(private val context: Context) {

    private var cache: List<RadicalEntry>? = null

    suspend fun getAllRadicals(): List<RadicalEntry> =
        cache ?: load().also { cache = it }

    suspend fun getRadicalById(id: String): RadicalEntry? =
        getAllRadicals().find { it.id == id }

    suspend fun filterByPosition(position: String): List<RadicalEntry> =
        getAllRadicals().filter { it.position.equals(position, ignoreCase = true) }

    suspend fun search(query: String): List<RadicalEntry> {
        if (query.isBlank()) return getAllRadicals()
        val q = query.trim()
        return getAllRadicals().filter { r ->
            r.character.contains(q) ||
                r.meaning.contains(q, ignoreCase = true)
        }
    }

    private suspend fun load(): List<RadicalEntry> =
        withContext(Dispatchers.IO) { CsvParser.parseRadicals(context, "radicals.csv") }
}
