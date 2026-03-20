package com.example.personalproject.data.model

data class MiscEntry(
    val id: String,
    val type: String,
    val content: String,
    val kanjiReferences: List<String>,   // "|" delimited
    val vocabReferences: List<String>,   // "|" delimited
    val grammarReferences: List<String>, // "|" delimited
)
