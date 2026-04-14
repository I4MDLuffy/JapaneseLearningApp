package app.kotori.japanese.data.repository

import app.kotori.japanese.data.csv.CsvParser
import app.kotori.japanese.data.model.NounEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NounRepository() {

    private var cache: List<NounEntry>? = null

    suspend fun getAllNouns(): List<NounEntry> =
        cache ?: load().also { cache = it }

    suspend fun getNounById(id: String): NounEntry? =
        getAllNouns().find { it.id == id }

    suspend fun filterByJlpt(level: String): List<NounEntry> =
        getAllNouns().filter { it.jlptLevel == level }

    suspend fun filterByTheme(theme: String): List<NounEntry> =
        getAllNouns().filter { it.theme.equals(theme, ignoreCase = true) }

    suspend fun search(query: String): List<NounEntry> {
        if (query.isBlank()) return getAllNouns()
        val q = query.trim()
        return getAllNouns().filter { n ->
            n.kanji.contains(q) ||
                n.hiragana.contains(q) ||
                n.romaji.contains(q, ignoreCase = true) ||
                n.meaning.contains(q, ignoreCase = true)
        }
    }

    private suspend fun load(): List<NounEntry> =
        withContext(Dispatchers.IO) { CsvParser.parseNouns("nouns.csv") }
}
