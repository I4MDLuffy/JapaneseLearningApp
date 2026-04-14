package app.kotori.japanese.grammar.list.mvi

import app.kotori.japanese.data.model.GrammarEntry
import app.kotori.japanese.data.repository.GrammarRepository
import app.kotori.japanese.mvi.BaseViewModel

class GrammarListViewModel(
    private val repository: GrammarRepository,
) : BaseViewModel<GrammarListState, GrammarListAction>(GrammarListState()) {

    init {
        dispatchAction(GrammarListAction.LoadEntries)
    }

    override fun dispatchAction(action: GrammarListAction) {
        when (action) {
            GrammarListAction.LoadEntries -> loadEntries()
            is GrammarListAction.Search -> search(action.query)
            is GrammarListAction.FilterByJlpt -> filterByJlpt(action.level)
            GrammarListAction.ClearFilters -> clearFilters()
        }
    }

    private fun loadEntries() {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getAllGrammar() },
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

    private fun matchesSearch(entry: GrammarEntry, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim()
        return entry.title.contains(q, ignoreCase = true) ||
            entry.content.contains(q, ignoreCase = true) ||
            entry.category.contains(q, ignoreCase = true)
    }

    private fun matchesJlpt(entry: GrammarEntry, level: String?) =
        level == null || entry.jlptLevel == level
}
