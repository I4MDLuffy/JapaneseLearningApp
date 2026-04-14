package app.kotori.japanese.data.repository

import app.kotori.japanese.data.csv.CsvParser
import app.kotori.japanese.data.model.VerbEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VerbRepository() {

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
        withContext(Dispatchers.IO) { CsvParser.parseVerbs("verbs.csv") }
}
