package app.kotori.japanese.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "known_items",
    indices = [Index(value = ["type", "itemId"], unique = true)],
)
data class KnownItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,    // "kanji" | "verb" | "adjective" | "noun" | "grammar" | "phrase" | "vocab" | "radical"
    val itemId: String,
    val markedAt: Long = System.currentTimeMillis(),
    // SRS fields (SM-2 algorithm)
    val srsInterval: Int = 1,           // days until next review
    val srsRepetitions: Int = 0,        // consecutive correct reviews
    val srsEaseFactor: Float = 2.5f,    // SM-2 ease factor (min 1.3)
    val srsNextReview: Long = 0L,       // epoch millis when next review is due
)
