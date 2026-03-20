package com.example.personalproject.adjectives.list.mvi

import com.example.personalproject.data.model.AdjectiveEntry
import com.example.personalproject.data.repository.AdjectiveRepository
import com.example.personalproject.mvi.BaseViewModel

class AdjectiveListViewModel(
    private val repository: AdjectiveRepository,
) : BaseViewModel<AdjectiveListState, AdjectiveListAction>(AdjectiveListState()) {

    init {
        dispatchAction(AdjectiveListAction.LoadEntries)
    }

    override fun dispatchAction(action: AdjectiveListAction) {
        when (action) {
            AdjectiveListAction.LoadEntries -> loadEntries()
            is AdjectiveListAction.Search -> search(action.query)
            is AdjectiveListAction.FilterByJlpt -> filterByJlpt(action.level)
            AdjectiveListAction.ClearFilters -> clearFilters()
        }
    }

    private fun loadEntries() {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getAllAdjectives() },
            onSuccess = { entries ->
                updateState {
                    copy(
                        allEntries = entries,
                        displayedEntries = entries,
                        availableJlptLevels = entries.map { it.jlptLevel }.distinct().sorted(),
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

    private fun matchesSearch(entry: AdjectiveEntry, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim()
        return entry.kanji.contains(q) ||
            entry.hiragana.contains(q) ||
            entry.romaji.contains(q, ignoreCase = true) ||
            entry.meaning.contains(q, ignoreCase = true)
    }

    private fun matchesJlpt(entry: AdjectiveEntry, level: String?) =
        level == null || entry.jlptLevel == level
}
