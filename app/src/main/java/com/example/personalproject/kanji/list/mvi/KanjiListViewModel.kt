package app.kotori.japanese.kanji.list.mvi

import app.kotori.japanese.data.model.KanjiEntry
import app.kotori.japanese.data.repository.KanjiRepository
import app.kotori.japanese.mvi.BaseViewModel

class KanjiListViewModel(
    private val repository: KanjiRepository,
) : BaseViewModel<KanjiListState, KanjiListAction>(KanjiListState()) {

    init {
        dispatchAction(KanjiListAction.LoadEntries)
    }

    override fun dispatchAction(action: KanjiListAction) {
        when (action) {
            KanjiListAction.LoadEntries -> loadEntries()
            is KanjiListAction.Search -> search(action.query)
            is KanjiListAction.FilterByJlpt -> filterByJlpt(action.level)
            KanjiListAction.ClearFilters -> clearFilters()
        }
    }

    private fun loadEntries() {
        updateState { copy(isLoading = true) }
        execute(
            block = { repository.getAllKanji() },
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
        updateState {
            copy(searchQuery = "", selectedJlptLevel = null, displayedEntries = allEntries)
        }
    }

    private fun applyFilters() {
        val current = uiState.value
        val filtered = current.allEntries.filter { entry ->
            matchesSearch(entry, current.searchQuery) &&
                matchesJlpt(entry, current.selectedJlptLevel)
        }
        updateState { copy(displayedEntries = filtered) }
    }

    private fun matchesSearch(entry: KanjiEntry, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim()
        return entry.kanji.contains(q) ||
            entry.meaning.contains(q, ignoreCase = true) ||
            entry.onYomi.any { it.contains(q) } ||
            entry.kunYomi.any { it.contains(q) }
    }

    private fun matchesJlpt(entry: KanjiEntry, level: String?) =
        level == null || entry.jlptLevel == level
}
