package com.example.personalproject.ui.games.mvi

import com.example.personalproject.data.model.KanjiEntry
import com.example.personalproject.mvi.BaseState

data class KanjiBuilderTile(
    val radicalId: String,
    val character: String,
    val meaning: String,
)

sealed interface KanjiBuilderPhase {
    object Loading : KanjiBuilderPhase

    /**
     * Player sees a kanji + its meaning. A 9-tile grid of radical characters is shown.
     * The player must tap all radicals that are components of the kanji ([correctIds]).
     * Wrong taps flash red. When all correct tiles are selected the round advances.
     */
    data class Building(
        val entries: List<KanjiEntry>,
        val currentIndex: Int = 0,
        val tiles: List<KanjiBuilderTile> = emptyList(),
        val correctIds: Set<String> = emptySet(),
        val selectedIds: Set<String> = emptySet(),
        val wrongIds: Set<String> = emptySet(),
        val score: Int = 0,
    ) : KanjiBuilderPhase

    data class Results(val score: Int, val total: Int) : KanjiBuilderPhase
}

data class KanjiBuilderState(
    val phase: KanjiBuilderPhase = KanjiBuilderPhase.Loading,
) : BaseState
