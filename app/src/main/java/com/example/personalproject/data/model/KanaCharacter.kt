package com.example.personalproject.data.model

data class KanaCharacter(
    val id: String,
    val character: String,
    val romaji: String,
    val hiraganaOrKatakana: String,  // "hiragana" | "katakana"
    val group: String,
    val strokeOrder: String,
)
