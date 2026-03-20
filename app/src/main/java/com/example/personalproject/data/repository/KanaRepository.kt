package com.example.personalproject.data.repository

import android.content.Context
import com.example.personalproject.data.csv.CsvParser
import com.example.personalproject.data.model.KanaCharacter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KanaRepository(private val context: Context) {

    private var cache: List<KanaCharacter>? = null

    suspend fun getAllKana(): List<KanaCharacter> =
        cache ?: load().also { cache = it }

    suspend fun getKanaById(id: String): KanaCharacter? =
        getAllKana().find { it.id == id }

    suspend fun filterByType(type: String): List<KanaCharacter> =
        getAllKana().filter { it.hiraganaOrKatakana.equals(type, ignoreCase = true) }

    suspend fun filterByGroup(group: String): List<KanaCharacter> =
        getAllKana().filter { it.group.equals(group, ignoreCase = true) }

    private suspend fun load(): List<KanaCharacter> =
        withContext(Dispatchers.IO) { CsvParser.parseKana(context, "kana.csv") }
}
