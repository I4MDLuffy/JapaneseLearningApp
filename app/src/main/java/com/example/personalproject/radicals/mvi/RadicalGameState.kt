package app.kotori.japanese.radicals.mvi

import app.kotori.japanese.data.model.RadicalEntry
import app.kotori.japanese.mvi.BaseState

data class RadicalGameState(
    val phase: RadicalGamePhase = RadicalGamePhase.Loading,
) : BaseState

sealed interface RadicalGamePhase {
    object Loading : RadicalGamePhase

    data class Flashcard(
        val entries: List<RadicalEntry>,
        val currentIndex: Int,
        val isRevealed: Boolean,
    ) : RadicalGamePhase

    data class MultipleChoice(
        val entries: List<RadicalEntry>,
        val currentIndex: Int,
        val options: List<String>,
        val selectedOption: String?,
        val score: Int,
    ) : RadicalGamePhase

    data class Results(
        val score: Int,
        val total: Int,
    ) : RadicalGamePhase
}
