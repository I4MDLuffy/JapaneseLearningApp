package app.kotori.japanese.ui.basiccharacters

import app.kotori.japanese.data.kana.KanaEntry
import app.kotori.japanese.mvi.BaseState

enum class GameMode { MATCHING, TYPING, FLASHCARD, MULTIPLE_CHOICE, KANA_SPEED }

enum class TypingFeedback { CORRECT, WRONG }

data class MatchCard(
    val pairId: String,
    val text: String,
    val isKana: Boolean,
)

sealed interface KanaGamePhase {
    object ModeSelect : KanaGamePhase

    data class Matching(
        val cards: List<MatchCard>,
        val firstSelectedIndex: Int? = null,
        val matchedIds: Set<String> = emptySet(),
        val wrongIndices: Set<Int> = emptySet(),
        val batchIndex: Int = 0,
        val totalBatches: Int = 1,
        val totalScore: Int = 0,
    ) : KanaGamePhase

    data class Typing(
        val entries: List<KanaEntry>,
        val currentIndex: Int = 0,
        val inputText: String = "",
        val feedback: TypingFeedback? = null,
        val score: Int = 0,
    ) : KanaGamePhase

    data class Flashcard(
        val entries: List<KanaEntry>,
        val currentIndex: Int = 0,
        val revealed: Boolean = false,
        val correctCount: Int = 0,
        val incorrectCount: Int = 0,
    ) : KanaGamePhase

    data class MultipleChoice(
        val entries: List<KanaEntry>,
        val currentIndex: Int = 0,
        val options: List<String> = emptyList(),
        val selectedOption: String? = null,
        val isCorrect: Boolean? = null,
        val score: Int = 0,
    ) : KanaGamePhase

    data class KanaSpeed(
        val entries: List<KanaEntry>,
        val currentIndex: Int = 0,
        val inputText: String = "",
        val timeRemaining: Float = 1f,
        val feedback: TypingFeedback? = null,
        val score: Int = 0,
    ) : KanaGamePhase

    data class Results(
        val score: Int,
        val total: Int,
        val gameMode: GameMode,
    ) : KanaGamePhase
}

data class KanaGameState(
    val phase: KanaGamePhase = KanaGamePhase.ModeSelect,
) : BaseState
