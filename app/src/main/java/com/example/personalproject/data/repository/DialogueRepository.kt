package com.example.personalproject.data.repository

import android.content.Context
import com.example.personalproject.data.csv.CsvParser
import com.example.personalproject.data.model.DialogueEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DialogueRepository(private val context: Context) {

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
        withContext(Dispatchers.IO) { CsvParser.parseDialogues(context, "dialogue.csv") }
}
