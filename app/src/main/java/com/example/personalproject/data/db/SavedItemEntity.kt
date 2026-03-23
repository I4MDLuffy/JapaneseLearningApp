package com.example.personalproject.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_items",
    indices = [Index(value = ["type", "itemId"], unique = true)],
)
data class SavedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,       // "kanji" | "vocab" | "grammar" | "verb" | "adjective" | "noun" | "phrase"
    val itemId: String,     // original item ID from CSV
    val title: String,      // primary display text (kanji character, word, grammar keyword, etc.)
    val reading: String = "",   // hiragana or kana reading
    val meaning: String = "",   // English meaning
    val savedAt: Long = System.currentTimeMillis(),
)
