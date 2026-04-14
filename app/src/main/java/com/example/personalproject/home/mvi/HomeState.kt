package app.kotori.japanese.home.mvi

import app.kotori.japanese.data.model.VocabularyWord
import app.kotori.japanese.mvi.BaseState

data class HomeState(
    val wordOfTheDay: VocabularyWord? = null,
    val totalWords: Int = 0,
    val totalModules: Int = 0,
    val isLoading: Boolean = false,
) : BaseState
