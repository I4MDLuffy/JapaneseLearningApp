package com.example.personalproject.radicals.mvi

import com.example.personalproject.data.model.RadicalEntry
import com.example.personalproject.mvi.BaseState

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
