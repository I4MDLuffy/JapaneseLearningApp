package app.kotori.japanese.learning.mvi

import app.kotori.japanese.data.model.Chapter
import app.kotori.japanese.mvi.BaseState

data class LevelState(
    val levelName: String = "",
    val chapters: List<Chapter> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
) : BaseState
