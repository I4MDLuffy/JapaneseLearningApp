package app.kotori.japanese.data.repository

import app.kotori.japanese.data.csv.CsvParser
import app.kotori.japanese.data.model.KanjiEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KanjiRepository() {

    private var cache: List<KanjiEntry>? = null

    suspend fun getAllKanji(): List<KanjiEntry> =
        cache ?: load().also { cache = it }

    suspend fun getKanjiById(id: String): KanjiEntry? =
        getAllKanji().find { it.id == id }

    suspend fun filterByJlpt(level: String): List<KanjiEntry> =
        getAllKanji().filter { it.jlptLevel == level }

    suspend fun search(query: String): List<KanjiEntry> {
        if (query.isBlank()) return getAllKanji()
        val q = query.trim()
        return getAllKanji().filter { k ->
            k.kanji.contains(q) ||
                k.meaning.contains(q, ignoreCase = true) ||
                k.hiragana.contains(q) ||
                k.onYomi.any { it.contains(q, ignoreCase = true) } ||
                k.kunYomi.any { it.contains(q, ignoreCase = true) }
        }
    }

    private suspend fun load(): List<KanjiEntry> =
        withContext(Dispatchers.IO) { CsvParser.parseKanji("kanji.csv") }
}
