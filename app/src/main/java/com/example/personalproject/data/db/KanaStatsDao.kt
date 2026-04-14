package app.kotori.japanese.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface KanaStatsDao {

    @Query("SELECT * FROM kana_stats WHERE type = :type ORDER BY (incorrect * 1.0 / (correct + incorrect + 1)) DESC")
    suspend fun getStatsByType(type: String): List<KanaStatsEntity>

    @Query("SELECT * FROM kana_stats WHERE kana = :kana AND type = :type LIMIT 1")
    suspend fun getStats(kana: String, type: String): KanaStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: KanaStatsEntity)

    @Query("UPDATE kana_stats SET correct = correct + 1 WHERE kana = :kana AND type = :type")
    suspend fun incrementCorrect(kana: String, type: String)

    @Query("UPDATE kana_stats SET incorrect = incorrect + 1 WHERE kana = :kana AND type = :type")
    suspend fun incrementIncorrect(kana: String, type: String)
}
