package app.kotori.japanese.vocabulary.detail.mvi

import app.kotori.japanese.data.model.VocabularyWord
import app.kotori.japanese.mvi.BaseState

data class VocabularyDetailState(
    val word: VocabularyWord? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
