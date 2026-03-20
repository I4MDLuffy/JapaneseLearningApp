package com.example.personalproject.learning.mvi

import androidx.lifecycle.viewModelScope
import com.example.personalproject.data.model.ChapterType
import com.example.personalproject.data.repository.ChapterProgressRepository
import com.example.personalproject.data.repository.GrammarRepository
import com.example.personalproject.data.repository.VocabularyRepository
import com.example.personalproject.mvi.BaseViewModel
import kotlinx.coroutines.launch

private const val ITEMS_PER_CHAPTER = 5

class ChapterReaderViewModel(
    private val level: String,
    private val chapterIndex: Int,
    private val chapterType: ChapterType,
    private val setIndex: Int,
    private val chapterTitle: String,
    private val grammarRepository: GrammarRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: ChapterProgressRepository,
) : BaseViewModel<ChapterReaderState, ChapterReaderAction>(
    ChapterReaderState(chapterTitle = chapterTitle)
) {

    init {
        load()
    }

    override fun dispatchAction(action: ChapterReaderAction) {
        when (action) {
            is ChapterReaderAction.NextItem -> handleNextItem()
            is ChapterReaderAction.RevealStudyVocab -> updateState { copy(isRevealed = true) }
            is ChapterReaderAction.CompleteChapter -> handleComplete()
        }
    }

    private fun load() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            try {
                val jlpt = jlptForLevel(level)
                val items: List<ChapterItem> = when (chapterType) {
                    ChapterType.GRAMMAR -> {
                        grammarRepository.filterByJlpt(jlpt)
                            .sortedBy { it.difficultyOrder }
                            .chunked(ITEMS_PER_CHAPTER)
                            .getOrElse(setIndex) { emptyList() }
                            .map { ChapterItem.GrammarItem(it) }
                    }
                    ChapterType.VOCAB -> {
                        vocabularyRepository.filterByJlpt(jlpt)
                            .sortedBy { it.id }
                            .chunked(ITEMS_PER_CHAPTER)
                            .getOrElse(setIndex) { emptyList() }
                            .map { ChapterItem.VocabItem(it) }
                    }
                    ChapterType.STUDY_VOCAB -> {
                        vocabularyRepository.filterByJlpt(jlpt)
                            .sortedBy { it.id }
                            .chunked(ITEMS_PER_CHAPTER)
                            .getOrElse(setIndex) { emptyList() }
                            .map { ChapterItem.StudyVocabItem(it) }
                    }
                }
                updateState { copy(items = items, isLoading = false) }
            } catch (e: Exception) {
                updateState { copy(error = e.message ?: "Failed to load items", isLoading = false) }
            }
        }
    }

    private fun handleNextItem() {
        val state = uiState.value
        val next = state.currentIndex + 1
        if (next >= state.items.size) {
            handleComplete()
        } else {
            updateState { copy(currentIndex = next, isRevealed = false) }
        }
    }

    private fun handleComplete() {
        progressRepository.markCompleted(level, chapterIndex)
        updateState { copy(isCompleted = true) }
    }
}

private fun jlptForLevel(level: String) = when (level) {
    "beginner" -> "N5"
    "intermediate" -> "N4"
    "advanced" -> "N3"
    "master" -> "N2"
    else -> "N5"
}
