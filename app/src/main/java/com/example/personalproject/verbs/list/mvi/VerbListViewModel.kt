package app.kotori.japanese.verbs.list.mvi

import app.kotori.japanese.data.model.VerbEntry
import app.kotori.japanese.data.repository.VerbRepository
import app.kotori.japanese.mvi.BaseViewModel

class VerbListViewModel(
    private val repository: VerbRepository,
) : BaseViewModel<VerbListState, VerbListAction>(VerbListState()) {

    init {
        dispatchAction(VerbListAction.LoadEntries)
    }

    override fun dispatchAction(action: VerbListAction) {
        when (action) {
            VerbListAction.LoadEntries -> loadEntries()
            is VerbListAction.Search -> search(action.query)
            is VerbListAction.FilterByJlpt -> filterByJlpt(action.level)
            VerbListAction.ClearFilters -> clearFilters()
        }
    }

    private fun loadEntries() {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getAllVerbs() },
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

    private fun matchesSearch(entry: VerbEntry, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim()
        return entry.kanji.contains(q) ||
            entry.dictionaryForm.contains(q) ||
            entry.romaji.contains(q, ignoreCase = true) ||
            entry.meaning.contains(q, ignoreCase = true)
    }

    private fun matchesJlpt(entry: VerbEntry, level: String?) =
        level == null || entry.jlptLevel == level
}
