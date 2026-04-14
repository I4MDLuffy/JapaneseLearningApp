package app.kotori.japanese.data.repository

import app.kotori.japanese.data.csv.CsvParser
import app.kotori.japanese.data.model.DialogueEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DialogueRepository() {

    private var cache: List<DialogueEntry>? = null

    suspend fun getAllDialogues(): List<DialogueEntry> =
        cache ?: load().also { cache = it }

    suspend fun getDialogueById(id: String): DialogueEntry? =
        getAllDialogues().find { it.id == id }

    suspend fun search(query: String): List<DialogueEntry> {
        if (query.isBlank()) return getAllDialogues()
        val q = query.trim()
        return getAllDialogues().filter { d ->
            d.japaneseContent.contains(q) ||
                d.englishContent.contains(q, ignoreCase = true)
        }
    }

    private suspend fun load(): List<DialogueEntry> =
        withContext(Dispatchers.IO) { CsvParser.parseDialogues("dialogue.csv") }
}
