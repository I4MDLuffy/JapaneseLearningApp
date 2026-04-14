package app.kotori.japanese.data.model

data class VerbEntry(
    val id: String,
    val kanji: String,                      // kanji form e.g. 食べる
    val kanjiRadical: String,
    val dictionaryForm: String,             // hiragana dictionary form
    val romaji: String,
    val stem: String,
    val transitivity: String,              // transitive / intransitive
    val hiragana: String,
    val meaning: String,
    val verbType: String,                  // u / ru / irr
    val theme: String,
    // ── Long-form (polite) conjugations ──────────────────────────────────────
    val presentAffirmative: String,        // e.g. たべます
    val presentNegative: String,           // e.g. たべません
    val pastAffirmative: String,           // e.g. たべました
    val pastNegative: String,              // e.g. たべませんでした
    // ── Te-form ──────────────────────────────────────────────────────────────
    val teFormAffirmative: String,         // e.g. たべて
    // ── Short-form (plain) conjugations ──────────────────────────────────────
    val presentShortNegative: String,      // e.g. たべない
    val teFormNegativeNaide: String,       // e.g. たべないで
    val teFormNegativeNakute: String,      // e.g. たべなくて
    val pastShortAffirmative: String,      // e.g. たべた
    val pastShortNegative: String,         // e.g. たべなかった
    // ── Other forms ──────────────────────────────────────────────────────────
    val tai: String,                       // e.g. たべたい
    val volitional: String,                // e.g. たべよう
    val baFormAffirmative: String,         // e.g. たべれば
    val baFormNegative: String,            // e.g. たべなければ
    val potential: String,                 // e.g. たべられる
    val causative: String,                 // e.g. たべさせる
    val passive: String,                   // e.g. たべられる
    val causativePassive: String,          // e.g. たべさせられる
    // ── References ───────────────────────────────────────────────────────────
    val grammarReferences: List<String>,   // "|" delimited
    val kanjiReferences: List<String>,     // "|" delimited
    val jlptLevel: String,
)
