package app.kotori.japanese.nouns.list.mvi

import app.kotori.japanese.data.model.NounEntry
import app.kotori.japanese.data.repository.NounRepository
import app.kotori.japanese.mvi.BaseViewModel

class NounListViewModel(
    private val repository: NounRepository,
) : BaseViewModel<NounListState, NounListAction>(NounListState()) {

    init {
        dispatchAction(NounListAction.LoadEntries)
    }

    override fun dispatchAction(action: NounListAction) {
        when (action) {
            NounListAction.LoadEntries -> loadEntries()
            is NounListAction.Search -> search(action.query)
            is NounListAction.FilterByJlpt -> filterByJlpt(action.level)
            NounListAction.ClearFilters -> clearFilters()
        }
    }

    private fun loadEntries() {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getAllNouns() },
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

    private fun matchesSearch(entry: NounEntry, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim()
        return entry.kanji.contains(q) ||
            entry.hiragana.contains(q) ||
            entry.romaji.contains(q, ignoreCase = true) ||
            entry.meaning.contains(q, ignoreCase = true)
    }

    private fun matchesJlpt(entry: NounEntry, level: String?) =
        level == null || entry.jlptLevel == level
}
