package app.kotori.japanese.data.repository

import app.kotori.japanese.data.csv.CsvParser
import app.kotori.japanese.data.model.MiscEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MiscRepository() {

    private var cache: List<MiscEntry>? = null

    suspend fun getAllMisc(): List<MiscEntry> =
        cache ?: load().also { cache = it }

    suspend fun getMiscById(id: String): MiscEntry? =
        getAllMisc().find { it.id == id }

    suspend fun filterByType(type: String): List<MiscEntry> =
        getAllMisc().filter { it.type.equals(type, ignoreCase = true) }

    private suspend fun load(): List<MiscEntry> =
        withContext(Dispatchers.IO) { CsvParser.parseMisc("miscellaneous.csv") }
}
