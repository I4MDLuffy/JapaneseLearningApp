package com.example.personalproject.data.repository

import android.content.Context
import com.example.personalproject.data.csv.CsvParser
import com.example.personalproject.data.model.VocabularyWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VocabularyRepository(private val context: Context) {

    private var cache: List<VocabularyWord>? = null

    suspend fun getAllWords(): List<VocabularyWord> =
        cache ?: load().also { cache = it }

    suspend fun getWordById(id: String): VocabularyWord? =
        getAllWords().find { it.id == id }

    suspend fun search(query: String): List<VocabularyWord> {
        if (query.isBlank()) return getAllWords()
        val q = query.trim()
        return getAllWords().filter { w ->
            w.japanese.contains(q, ignoreCase = true) ||
                w.hiragana.contains(q) ||
                w.romaji.contains(q, ignoreCase = true) ||
                w.english.contains(q, ignoreCase = true)
        }
    }

    suspend fun filterByJlpt(level: String): List<VocabularyWord> =
        getAllWords().filter { it.jlptLevel == level }

    suspend fun filterByPartOfSpeech(pos: String): List<VocabularyWord> =
        getAllWords().filter { it.partOfSpeech.equals(pos, ignoreCase = true) }

    /** Swap in a different CSV file name to load an alternate word list. */
    suspend fun loadFile(fileName: String): List<VocabularyWord> =
        withContext(Dispatchers.IO) {
            CsvParser.parseVocabulary(context, fileName).also { cache = it }
        }

    private suspend fun load(): List<VocabularyWord> =
        withContext(Dispatchers.IO) {
            CsvParser.parseVocabulary(context, "vocabulary.csv")
        }
}
