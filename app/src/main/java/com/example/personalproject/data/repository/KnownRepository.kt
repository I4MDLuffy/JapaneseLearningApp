package com.example.personalproject.data.repository

import com.example.personalproject.data.db.KnownDao
import com.example.personalproject.data.db.KnownItemEntity
import com.example.personalproject.data.db.KotobaDatabase
import kotlinx.coroutines.flow.Flow

class KnownRepository(private val dao: KnownDao) {

    /** Reactive known-state flow for use in detail screens. */
    fun isItemKnownFlow(type: String, itemId: String): Flow<Boolean> =
        dao.isItemKnown(type, itemId)

    /** Reactive count of known items for a given type — used in TermStudy percentage display. */
    fun getKnownCount(type: String): Flow<Int> =
        dao.getKnownCount(type)

    /** Reactive count of SRS items due for review right now. */
    fun getDueCount(): Flow<Int> =
        dao.getDueCount(System.currentTimeMillis())

    /** Returns items due for SRS review (up to [limit]). */
    suspend fun getDueItems(limit: Int = 30): List<KnownItemEntity> =
        dao.getDueItems(System.currentTimeMillis(), limit)

    /**
     * Toggle known state. Returns true if the item is now known.
     * Items newly marked as known are scheduled for first review immediately.
     */
    suspend fun toggle(type: String, itemId: String): Boolean {
        return if (dao.isItemKnownOnce(type, itemId)) {
            dao.unmarkKnown(type, itemId)
            false
        } else {
            dao.markKnown(
                KnownItemEntity(
                    type = type,
                    itemId = itemId,
                    srsNextReview = System.currentTimeMillis(), // due immediately for first review
                )
            )
            true
        }
    }

    /**
     * Record the result of an SRS review for the given item.
     * Updates the interval, repetitions, ease factor, and next-review timestamp
     * using a simplified SM-2 algorithm.
     */
    suspend fun recordReview(type: String, itemId: String, correct: Boolean) {
        val item = dao.getItemOnce(type, itemId) ?: return
        val (interval, reps, ease) = computeNextSrs(
            currentInterval = item.srsInterval,
            reps = item.srsRepetitions,
            ease = item.srsEaseFactor,
            correct = correct,
        )
        val nextReview = System.currentTimeMillis() + interval * DAY_MS
        dao.updateSrs(type, itemId, interval, reps, ease, nextReview)
    }

    // ── SM-2 algorithm ─────────────────────────────────────────────────────────

    private fun computeNextSrs(
        currentInterval: Int,
        reps: Int,
        ease: Float,
        correct: Boolean,
    ): Triple<Int, Int, Float> {
        return if (correct) {
            val newEase = (ease + 0.1f).coerceAtLeast(MIN_EASE)
            val newReps = reps + 1
            val newInterval = when {
                newReps == 1 -> 1
                newReps == 2 -> 6
                else -> (currentInterval * ease).toInt().coerceAtLeast(1)
            }
            Triple(newInterval, newReps, newEase)
        } else {
            val newEase = (ease - 0.2f).coerceAtLeast(MIN_EASE)
            Triple(1, 0, newEase)
        }
    }

    companion object {
        fun create(db: KotobaDatabase): KnownRepository = KnownRepository(db.knownDao())
        private const val MIN_EASE = 1.3f
        private const val DAY_MS = 24L * 60 * 60 * 1000
    }
}
