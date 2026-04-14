package com.example.personalproject.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KnownDao {

    @Query("SELECT EXISTS(SELECT 1 FROM known_items WHERE type = :type AND itemId = :itemId)")
    fun isItemKnown(type: String, itemId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM known_items WHERE type = :type AND itemId = :itemId)")
    suspend fun isItemKnownOnce(type: String, itemId: String): Boolean

    @Query("SELECT COUNT(*) FROM known_items WHERE type = :type")
    fun getKnownCount(type: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markKnown(item: KnownItemEntity): Long

    @Query("DELETE FROM known_items WHERE type = :type AND itemId = :itemId")
    suspend fun unmarkKnown(type: String, itemId: String)

    // ── SRS queries ────────────────────────────────────────────────────────────

    @Query("SELECT * FROM known_items WHERE type = :type AND itemId = :itemId LIMIT 1")
    suspend fun getItemOnce(type: String, itemId: String): KnownItemEntity?

    /** Items due for review (srsNextReview <= now). Ordered soonest-due first. */
    @Query("SELECT * FROM known_items WHERE srsNextReview <= :nowMillis ORDER BY srsNextReview ASC LIMIT :limit")
    suspend fun getDueItems(nowMillis: Long, limit: Int): List<KnownItemEntity>

    /** Reactive count of items due for review — shown on home screen. */
    @Query("SELECT COUNT(*) FROM known_items WHERE srsNextReview <= :nowMillis")
    fun getDueCount(nowMillis: Long): Flow<Int>

    @Query("""
        UPDATE known_items
        SET srsInterval = :interval, srsRepetitions = :reps, srsEaseFactor = :ease, srsNextReview = :nextReview
        WHERE type = :type AND itemId = :itemId
    """)
    suspend fun updateSrs(type: String, itemId: String, interval: Int, reps: Int, ease: Float, nextReview: Long)
}
