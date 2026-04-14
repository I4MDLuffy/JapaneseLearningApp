package com.example.personalproject.phrases.list.mvi

import com.example.personalproject.data.model.PhraseEntry
import com.example.personalproject.data.repository.PhraseRepository
import com.example.personalproject.mvi.BaseViewModel

class PhraseListViewModel(
    private val repository: PhraseRepository,
    private val initialCategory: String = "",
) : BaseViewModel<PhraseListState, PhraseListAction>(PhraseListState()) {

    init {
        dispatchAction(PhraseListAction.LoadEntries)
    }

    override fun dispatchAction(action: PhraseListAction) {
        when (action) {
            PhraseListAction.LoadEntries -> loadEntries()
            is PhraseListAction.Search -> search(action.query)
            is PhraseListAction.FilterByJlpt -> filterByJlpt(action.level)
            PhraseListAction.ClearFilters -> clearFilters()
        }
    }

    private fun loadEntries() {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getAllPhrases() },
            onSuccess = { entries ->
                val base = if (initialCategory.isBlank()) entries
                           else entries.filter { it.category.equals(initialCategory, ignoreCase = true) }
                updateState {
                    copy(
                        allEntries = base,
                        displayedEntries = base,
                        availableJlptLevels = base.map { it.jlptLevel }.distinct().sorted(),
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

    private fun clearFilters() {
        updateState { copy(searchQuery = "", selectedJlptLevel = null, displayedEntries = allEntries) }
    }

    private fun applyFilters() {
        val current = uiState.value
        val filtered = current.allEntries.filter { entry ->
            matchesSearch(entry, current.searchQuery) &&
                matchesJlpt(entry, current.selectedJlptLevel)
        }
        updateState { copy(displayedEntries = filtered) }
    }

    private fun matchesSearch(entry: PhraseEntry, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim()
        return entry.phrase.contains(q) ||
            entry.reading.contains(q) ||
            entry.romaji.contains(q, ignoreCase = true) ||
            entry.meaning.contains(q, ignoreCase = true)
    }

    private fun matchesJlpt(entry: PhraseEntry, level: String?) =
        level == null || entry.jlptLevel == level
}
