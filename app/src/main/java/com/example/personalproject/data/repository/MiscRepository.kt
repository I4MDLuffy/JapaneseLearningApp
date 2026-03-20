package com.example.personalproject.data.repository

import android.content.Context
import com.example.personalproject.data.csv.CsvParser
import com.example.personalproject.data.model.MiscEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MiscRepository(private val context: Context) {

    private var cache: List<MiscEntry>? = null

    suspend fun getAllMisc(): List<MiscEntry> =
        cache ?: load().also { cache = it }

    suspend fun getMiscById(id: String): MiscEntry? =
        getAllMisc().find { it.id == id }

    suspend fun filterByType(type: String): List<MiscEntry> =
        getAllMisc().filter { it.type.equals(type, ignoreCase = true) }

    private suspend fun load(): List<MiscEntry> =
        withContext(Dispatchers.IO) { CsvParser.parseMisc(context, "miscellaneous.csv") }
}
