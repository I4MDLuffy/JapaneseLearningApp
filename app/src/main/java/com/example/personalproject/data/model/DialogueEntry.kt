package com.example.personalproject.data.model

data class DialogueEntry(
    val id: String,
    val japaneseContent: String,
    val englishContent: String,
    val kanjiReferences: List<String>,  // "|" delimited
    val vocabReferences: List<String>,  // "|" delimited
    val reading: String,
    val romaji: String,
)
