package com.example.personalproject.data.model

data class AdjectiveEntry(
    val id: String,
    val kanji: String,
    val hiragana: String,
    val romaji: String,
    val examplePhraseReferences: List<String>,  // "|" delimited
    val meaning: String,
    val adjType: String,                        // i / na
    val theme: String,
    // ── Past ─────────────────────────────────────────────────────────────────
    val pastAffirmative: String,
    val pastNegative: String,
    val pastAffirmativeShort: String,
    val pastNegativeShort: String,
    // ── Present ──────────────────────────────────────────────────────────────
    val presentAffirmative: String,
    val presentNegative: String,
    val presentNegativeShort: String,
    // ── Te / Naru ─────────────────────────────────────────────────────────────
    val teFormAffirmative: String,
    val teFormNegative: String,
    val adjNaru: String,                        // adj + なる (only filled for na-adj)
    // ── References ───────────────────────────────────────────────────────────
    val grammarReferences: List<String>,
    val kanjiReferences: List<String>,
    val jlptLevel: String,
    val unlockedAtGrammarId: String,
)
