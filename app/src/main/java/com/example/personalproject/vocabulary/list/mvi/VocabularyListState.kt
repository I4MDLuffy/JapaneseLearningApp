package app.kotori.japanese.vocabulary.list.mvi

import app.kotori.japanese.data.model.VocabularyWord
import app.kotori.japanese.mvi.BaseState

data class VocabularyListState(
    val allWords: List<VocabularyWord> = emptyList(),
    val displayedWords: List<VocabularyWord> = emptyList(),
    val searchQuery: String = "",
    val selectedJlptLevel: String? = null,
    val selectedPartOfSpeech: String? = null,
    val availableJlptLevels: List<String> = emptyList(),
    val availablePartsOfSpeech: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
) : BaseState
