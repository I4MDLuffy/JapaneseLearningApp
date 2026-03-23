package com.example.personalproject.ui.games.mvi

import com.example.personalproject.data.model.KanjiEntry
import com.example.personalproject.mvi.BaseState

enum class KanjiDropMode { HIRAGANA, ROMAJI }

sealed interface KanjiDropPhase {
    object Loading : KanjiDropPhase

    /**
     * A kanji character falls from top to bottom over [dropProgress] 0→1.
     * Player picks the correct reading from [options] before the timer expires.
     * Three lives — lose one per miss or time-out.
     */
    data class Playing(
        val entries: List<KanjiEntry>,
        val currentIndex: Int = 0,
        val dropProgress: Float = 0f,
        val options: List<String> = emptyList(),
        val selectedOption: String? = null,
        val lives: Int = 3,
        val score: Int = 0,
    ) : KanjiDropPhase

    data class Results(val score: Int, val total: Int) : KanjiDropPhase
}

data class KanjiDropState(
    val phase: KanjiDropPhase = KanjiDropPhase.Loading,
    val mode: KanjiDropMode = KanjiDropMode.HIRAGANA,
) : BaseState
