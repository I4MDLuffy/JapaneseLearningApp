package com.example.personalproject.vocabulary.list.mvi

import com.example.personalproject.data.model.VocabularyWord
import com.example.personalproject.data.repository.VocabularyRepository
import com.example.personalproject.mvi.BaseViewModel

class VocabularyListViewModel(
    private val repository: VocabularyRepository,
) : BaseViewModel<VocabularyListState, VocabularyListAction>(VocabularyListState()) {

    init {
        dispatchAction(VocabularyListAction.LoadWords)
    }

    override fun dispatchAction(action: VocabularyListAction) {
        when (action) {
            VocabularyListAction.LoadWords -> loadWords()
            is VocabularyListAction.Search -> search(action.query)
            is VocabularyListAction.FilterByJlpt -> filterByJlpt(action.level)
            is VocabularyListAction.FilterByPartOfSpeech -> filterByPos(action.pos)
            VocabularyListAction.ClearFilters -> clearFilters()
        }
    }

    private fun loadWords() {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getAllWords() },
            onSuccess = { words ->
                updateState {
                    copy(
                        allWords = words,
                        displayedWords = words,
                        availableJlptLevels = words.map { it.jlptLevel }.distinct().sorted(),
                        availablePartsOfSpeech = words.map { it.partOfSpeech }.distinct().sorted(),
                        isLoading = false,
                    )
                }
            },
            onError = { e -> updateState { copy(isLoading = false, error = e.message) } },
        )
    }

    private fun search(query: String) {
        updateState { copy(searchQuery = query) }
        applyFilters()
    }

    private fun filterByJlpt(level: String?) {
        updateState { copy(selectedJlptLevel = level) }
        applyFilters()
    }

    private fun filterByPos(pos: String?) {
        updateState { copy(selectedPartOfSpeech = pos) }
        applyFilters()
    }

    private fun clearFilters() {
        updateState {
            copy(
                searchQuery = "",
                selectedJlptLevel = null,
                selectedPartOfSpeech = null,
                displayedWords = allWords,
            )
        }
    }

    private fun applyFilters() {
        val current = uiState.value
        val filtered = current.allWords.filter { word ->
            matchesSearch(word, current.searchQuery) &&
                matchesJlpt(word, current.selectedJlptLevel) &&
                matchesPos(word, current.selectedPartOfSpeech)
        }
        updateState { copy(displayedWords = filtered) }
    }

    private fun matchesSearch(word: VocabularyWord, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim()
        return word.japanese.contains(q, ignoreCase = true) ||
            word.hiragana.contains(q) ||
            word.romaji.contains(q, ignoreCase = true) ||
            word.english.contains(q, ignoreCase = true)
    }

    private fun matchesJlpt(word: VocabularyWord, level: String?) =
        level == null || word.jlptLevel == level

    private fun matchesPos(word: VocabularyWord, pos: String?) =
        pos == null || word.partOfSpeech.equals(pos, ignoreCase = true)
}
