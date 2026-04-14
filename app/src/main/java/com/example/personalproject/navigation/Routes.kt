package com.example.personalproject.navigation

import kotlinx.serialization.Serializable

// ── Top-level ─────────────────────────────────────────────────────────────────

@Serializable data object OpeningRoute
@Serializable data object IntroductionRoute
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
@Serializable data class DialogueDetailRoute(val conversationTitle: String, val dialogueIds: String)

// ── Kanji ─────────────────────────────────────────────────────────────────────

@Serializable data object KanjiListRoute
@Serializable data class KanjiDetailRoute(val kanjiId: String, val allIds: String = "")

// ── Verbs ─────────────────────────────────────────────────────────────────────

@Serializable data object VerbListRoute
@Serializable data class VerbDetailRoute(val verbId: String, val allIds: String = "")

// ── Adjectives ────────────────────────────────────────────────────────────────

@Serializable data object AdjectiveListRoute
@Serializable data class AdjectiveDetailRoute(val adjId: String, val allIds: String = "")

// ── Nouns ─────────────────────────────────────────────────────────────────────

@Serializable data object NounListRoute
@Serializable data class NounDetailRoute(val nounId: String, val allIds: String = "")

// ── Grammar ───────────────────────────────────────────────────────────────────

@Serializable data object GrammarListRoute
@Serializable data class GrammarDetailRoute(val grammarId: String, val allIds: String = "")

// ── Phrases ───────────────────────────────────────────────────────────────────

@Serializable data class PhraseListRoute(val category: String = "")
@Serializable data class PhraseDetailRoute(val phraseId: String, val allIds: String = "")

// ── Radicals ──────────────────────────────────────────────────────────────────

@Serializable data object RadicalListRoute
@Serializable data class RadicalDetailRoute(val radicalId: String, val allIds: String = "")
@Serializable data class RadicalKanjiListRoute(val radicalId: String)
@Serializable data class RadicalGameRoute(val groupId: String) // "all" or stroke-count group e.g. "2"

// ── Study game term selection ──────────────────────────────────────────────────

@Serializable data class GameSetupRoute(val gameType: String) // lesson + term type selection

// ── Verb conjugation game ──────────────────────────────────────────────────────

@Serializable data object VerbConjugationSetupRoute
@Serializable data class VerbConjugationGameRoute(val level: String, val formKeys: String, val count: Int)

// ── Grammar fill-in-blank ──────────────────────────────────────────────────────

@Serializable data object GrammarFillInRoute

// ── JLPT ─────────────────────────────────────────────────────────────────────

@Serializable data object JlptRoute
@Serializable data class JlptLevelRoute(val level: String)   // "N5" | "N4" | "N3" | "N2" | "N1"
@Serializable data class JlptPracticeTestRoute(val level: String)

// ── Onomatopoeia ──────────────────────────────────────────────────────────────

@Serializable data object OnomatopoeiaRoute

// ── Saved & Games ─────────────────────────────────────────────────────────────

@Serializable data object SavedRoute
@Serializable data object StudyGamesRoute
@Serializable data class StudyGameRoute(val gameType: String, val setKey: String)

// ── SRS Review ───────────────────────────────────────────────────────────────

@Serializable data object ReviewRoute

// ── Progress dashboard ────────────────────────────────────────────────────────

@Serializable data object ProgressRoute

// ── Kana writing game ─────────────────────────────────────────────────────────

@Serializable data class KanaWritingGameRoute(
    val kanaType: String,  // "hiragana" | "katakana" | "both"
)

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
