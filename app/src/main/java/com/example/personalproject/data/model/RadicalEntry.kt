package com.example.personalproject.data.model

data class RadicalEntry(
    val id: String,
    val character: String,
    val meaning: String,
    val strokeCount: Int,
    val position: String,
    val frequency: String,
    val exampleKanji: List<String>,    // "|" delimited
    val kanjiReferences: List<String>, // "|" delimited kanji ids
    val variantForms: List<String>,    // "|" delimited
)
