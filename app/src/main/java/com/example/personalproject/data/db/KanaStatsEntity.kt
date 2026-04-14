package app.kotori.japanese.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks per-character correct/incorrect answer counts for hiragana and katakana.
 * [kana] is the hiragana or katakana character (e.g. "あ", "ア").
 * [type] is "hiragana" or "katakana".
 */
@Entity(tableName = "kana_stats")
data class KanaStatsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kana: String,
    val type: String,          // "hiragana" | "katakana"
    val correct: Int = 0,
    val incorrect: Int = 0,
)
