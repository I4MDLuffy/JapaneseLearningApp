package com.example.personalproject.navigation

import kotlinx.serialization.Serializable

// ── Top-level ─────────────────────────────────────────────────────────────────

@Serializable data object OpeningRoute
@Serializable data object HomeRoute

// ── Basic Characters ──────────────────────────────────────────────────────────

@Serializable data object BasicCharactersRoute
@Serializable data object HiraganaRoute
@Serializable data object KatakanaRoute
@Serializable data class KanaGroupGameRoute(
    val kanaType: String,   // "hiragana" | "katakana"
    val groupId: String,    // group id | "all"
)

// ── Structured learning levels ────────────────────────────────────────────────

@Serializable data object BeginnerRoute
@Serializable data object IntermediateRoute
@Serializable data object AdvancedRoute
@Serializable data object MasterRoute

@Serializable data class ChapterReaderRoute(
    val level: String,
    val chapterIndex: Int,
    val chapterType: String,
    val setIndex: Int,
    val chapterTitle: String,
)

// ── Explore / Misc ────────────────────────────────────────────────────────────

@Serializable data object PurelyGrammarRoute
@Serializable data object QuickConversationalRoute
@Serializable data object CountersRoute
@Serializable data object TermStudyRoute
@Serializable data object DialogueReadingRoute

// ── Kanji ─────────────────────────────────────────────────────────────────────

@Serializable data object KanjiListRoute
@Serializable data class KanjiDetailRoute(val kanjiId: String)

// ── Verbs ─────────────────────────────────────────────────────────────────────

@Serializable data object VerbListRoute
@Serializable data class VerbDetailRoute(val verbId: String)

// ── Adjectives ────────────────────────────────────────────────────────────────

@Serializable data object AdjectiveListRoute
@Serializable data class AdjectiveDetailRoute(val adjId: String)

// ── Nouns ─────────────────────────────────────────────────────────────────────

@Serializable data object NounListRoute
@Serializable data class NounDetailRoute(val nounId: String)

// ── Grammar ───────────────────────────────────────────────────────────────────

@Serializable data object GrammarListRoute
@Serializable data class GrammarDetailRoute(val grammarId: String)

// ── Phrases ───────────────────────────────────────────────────────────────────

@Serializable data object PhraseListRoute
@Serializable data class PhraseDetailRoute(val phraseId: String)

// ── Saved & Games ─────────────────────────────────────────────────────────────

@Serializable data object SavedRoute
@Serializable data object StudyGamesRoute

// ── Settings ──────────────────────────────────────────────────────────────────

@Serializable data object SettingsRoute

// ── Legacy (kept until replaced) ──────────────────────────────────────────────

@Serializable data object NavHubRoute
@Serializable data object GettingStartedRoute
@Serializable data object MiscRoute
@Serializable data object VocabularyListRoute
@Serializable data class VocabularyDetailRoute(val wordId: String)
@Serializable data object ModulesRoute
@Serializable data class ModuleDetailRoute(val moduleId: String)
