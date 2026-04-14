package com.example.personalproject.ui.games.mvi

import com.example.personalproject.mvi.BaseState

enum class GameType { FLASHCARDS, TIMED_QUIZ, MATCH_PAIRS, KANA_SPEED, KANA_SWIPE, KANJI_DROP, KANJI_BUILDER, FILL_BLANK }
enum class PairMode { ENGLISH, ROMAJI }
enum class StudyFeedback { CORRECT, WRONG }
enum class FillBlankDirection { JP_TO_EN, EN_TO_JP }

data class StudyMatchCard(
    val pairId: String,
    val text: String,
    val isJapanese: Boolean,
)

sealed interface StudyGamePhase {
    object Loading : StudyGamePhase

    data class Flashcard(
        val entries: List<StudyItem>,
        val currentIndex: Int = 0,
        val isRevealed: Boolean = false,
        val gotItCount: Int = 0,
    ) : StudyGamePhase

    data class TimedQuiz(
        val entries: List<StudyItem>,
        val currentIndex: Int = 0,
        val options: List<String> = emptyList(),
        val selectedOption: String? = null,
        val isCorrect: Boolean? = null,
        val score: Int = 0,
        val timeRemaining: Float = 1f,
    ) : StudyGamePhase

    data class MatchPairs(
        val entries: List<StudyItem>,
        val cards: List<StudyMatchCard>,
        val firstSelectedIndex: Int? = null,
        val matchedIds: Set<String> = emptySet(),
        val wrongIndices: Set<Int> = emptySet(),
        val batchIndex: Int = 0,
        val totalBatches: Int = 1,
        val totalScore: Int = 0,
    ) : StudyGamePhase

    /**
     * Speed Round: show a vocabulary item, tap hiragana tiles in order to spell its reading.
     */
    data class KanaSpeed(
        val entries: List<StudyItem>,
        val currentIndex: Int = 0,
        val gridTiles: List<String> = emptyList(),
        val tappedIndices: List<Int> = emptyList(),
        val timeRemaining: Float = 1f,
        val feedback: StudyFeedback? = null,
        val score: Int = 0,
    ) : StudyGamePhase

    /**
     * Kana Swipe: show an item in English, tap hiragana tiles in order to spell its reading.
     */
    data class KanaSwipe(
        val entries: List<StudyItem>,
        val currentIndex: Int = 0,
        val tiles: List<String> = emptyList(),
        val path: List<Int> = emptyList(),
        val feedback: StudyFeedback? = null,
        val score: Int = 0,
    ) : StudyGamePhase

    data class FillBlank(
        val entries: List<StudyItem>,
        val currentIndex: Int = 0,
        val direction: FillBlankDirection = FillBlankDirection.JP_TO_EN,
        val inputText: String = "",
        val isSubmitted: Boolean = false,
        val isCorrect: Boolean? = null,
        val score: Int = 0,
    ) : StudyGamePhase

    data class Results(
        val score: Int,
        val total: Int,
        val gameType: GameType,
    ) : StudyGamePhase
}

data class StudyGameState(
    val phase: StudyGamePhase = StudyGamePhase.Loading,
    val pairMode: PairMode = PairMode.ENGLISH,
    val error: String? = null,
) : BaseState
