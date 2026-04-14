package com.example.personalproject.data.repository

import com.example.personalproject.data.db.KanaStatsDao
import com.example.personalproject.data.db.KanaStatsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KanaStatsRepository(private val dao: KanaStatsDao) {

    suspend fun recordAnswer(kana: String, type: String, correct: Boolean) =
        withContext(Dispatchers.IO) {
            // Ensure row exists
            if (dao.getStats(kana, type) == null) {
                dao.upsert(KanaStatsEntity(kana = kana, type = type))
            }
            if (correct) dao.incrementCorrect(kana, type)
            else dao.incrementIncorrect(kana, type)
        }

    /** Returns stats sorted by weakness (highest error rate first). */
    suspend fun getStatsByType(type: String): List<KanaStatsEntity> =
        withContext(Dispatchers.IO) { dao.getStatsByType(type) }

    companion object {
        fun create(dao: KanaStatsDao) = KanaStatsRepository(dao)
    }
}
