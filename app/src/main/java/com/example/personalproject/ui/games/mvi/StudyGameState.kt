package com.example.personalproject.ui.games.mvi

import com.example.personalproject.data.model.VocabularyWord
import com.example.personalproject.mvi.BaseState

enum class GameType { FLASHCARDS, TIMED_QUIZ, MATCH_PAIRS, KANA_SPEED, KANA_SWIPE, KANJI_DROP, KANJI_BUILDER }
enum class PairMode { ENGLISH, ROMAJI }
enum class StudyFeedback { CORRECT, WRONG }

data class StudyMatchCard(
    val pairId: String,
    val text: String,
    val isJapanese: Boolean,
)

sealed interface StudyGamePhase {
    object Loading : StudyGamePhase

    data class Flashcard(
        val entries: List<VocabularyWord>,
        val currentIndex: Int = 0,
        val isRevealed: Boolean = false,
        val gotItCount: Int = 0,
    ) : StudyGamePhase

    data class TimedQuiz(
        val entries: List<VocabularyWord>,
        val currentIndex: Int = 0,
        val options: List<String> = emptyList(),
        val selectedOption: String? = null,
        val isCorrect: Boolean? = null,
        val score: Int = 0,
        val timeRemaining: Float = 1f,
    ) : StudyGamePhase

    data class MatchPairs(
        val entries: List<VocabularyWord>,
        val cards: List<StudyMatchCard>,
        val firstSelectedIndex: Int? = null,
        val matchedIds: Set<String> = emptySet(),
        val wrongIndices: Set<Int> = emptySet(),
        val batchIndex: Int = 0,
        val totalBatches: Int = 1,
        val totalScore: Int = 0,
    ) : StudyGamePhase

    /**
     * Speed Round: show a vocabulary word, tap hiragana tiles in order to spell its reading.
     * Each tap is immediately validated against the next expected character.
     */
    data class KanaSpeed(
        val entries: List<VocabularyWord>,
        val currentIndex: Int = 0,
        val gridTiles: List<String> = emptyList(),    // 12-tile grid
        val tappedIndices: List<Int> = emptyList(),   // indices tapped so far (in order)
        val timeRemaining: Float = 1f,
        val feedback: StudyFeedback? = null,
        val score: Int = 0,
    ) : StudyGamePhase

    /**
     * Kana Swipe: show a vocabulary word in English, tap 8 hiragana tiles in order to spell
     * its Japanese reading. No timer — undo and submit when ready.
     */
    data class KanaSwipe(
        val entries: List<VocabularyWord>,
        val currentIndex: Int = 0,
        val tiles: List<String> = emptyList(),  // 8 hiragana tiles
        val path: List<Int> = emptyList(),       // tile indices in tap order
        val feedback: StudyFeedback? = null,
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
