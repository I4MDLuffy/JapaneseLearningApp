package com.example.personalproject.data.model

data class KanjiEntry(
    val id: String,
    val kanji: String,
    val meaning: String,
    val hiragana: String,
    val onYomi: List<String>,               // "|" delimited
    val kunYomi: List<String>,              // "|" delimited
    val vocabReferences: List<String>,      // "|" delimited vocab ids
    val jlptLevel: String,
    val theme: String,
    val unicode: String,
    val strokeOrderImage: String,
    val radicalReferences: List<String>,    // "|" delimited radical ids
    val strokeCount: Int,
    val gradeLevel: String,
    val componentStructure: String,
)
