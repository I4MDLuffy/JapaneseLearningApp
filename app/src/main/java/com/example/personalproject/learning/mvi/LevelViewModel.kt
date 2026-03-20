package com.example.personalproject.learning.mvi

import androidx.lifecycle.viewModelScope
import com.example.personalproject.data.model.Chapter
import com.example.personalproject.data.model.ChapterType
import com.example.personalproject.data.repository.ChapterProgressRepository
import com.example.personalproject.data.repository.GrammarRepository
import com.example.personalproject.data.repository.VocabularyRepository
import com.example.personalproject.mvi.BaseViewModel
import kotlinx.coroutines.launch

private const val ITEMS_PER_CHAPTER = 5

class LevelViewModel(
    private val level: String,
    private val grammarRepository: GrammarRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val progressRepository: ChapterProgressRepository,
) : BaseViewModel<LevelState, LevelAction>(LevelState(levelName = levelDisplayName(level))) {

    init {
        load()
    }

    override fun dispatchAction(action: LevelAction) {
        when (action) {
            is LevelAction.Load -> load()
        }
    }

    fun refresh() {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            try {
                val jlpt = jlptForLevel(level)
                val grammar = grammarRepository.filterByJlpt(jlpt).sortedBy { it.difficultyOrder }
                val vocab = vocabularyRepository.filterByJlpt(jlpt).sortedBy { it.id }

                val grammarChunks = grammar.chunked(ITEMS_PER_CHAPTER)
                val vocabChunks = vocab.chunked(ITEMS_PER_CHAPTER)
                val numSets = maxOf(grammarChunks.size, vocabChunks.size)

                val chapters = mutableListOf<Chapter>()
                var chapterIndex = 0
                for (setIndex in 0 until numSets) {
                    if (setIndex < grammarChunks.size) {
                        chapters.add(makeChapter(chapterIndex, ChapterType.GRAMMAR, setIndex, setIndex + 1))
                        chapterIndex++
                    }
                    if (setIndex < vocabChunks.size) {
                        chapters.add(makeChapter(chapterIndex, ChapterType.VOCAB, setIndex, setIndex + 1))
                        chapterIndex++
                    }
                    if (setIndex < vocabChunks.size) {
                        chapters.add(makeChapter(chapterIndex, ChapterType.STUDY_VOCAB, setIndex, setIndex + 1))
                        chapterIndex++
                    }
                }
                updateState { copy(chapters = chapters, isLoading = false) }
            } catch (e: Exception) {
                updateState { copy(error = e.message ?: "Failed to load chapters", isLoading = false) }
            }
        }
    }

    private fun makeChapter(index: Int, type: ChapterType, setIndex: Int, setNumber: Int): Chapter {
        val completed = progressRepository.isCompleted(level, index)
        val unlocked = index == 0 || progressRepository.isCompleted(level, index - 1)
        val title = when (type) {
            ChapterType.GRAMMAR -> "Grammar $setNumber"
            ChapterType.VOCAB -> "Vocabulary $setNumber"
            ChapterType.STUDY_VOCAB -> "Study Vocabulary $setNumber"
        }
        return Chapter(
            index = index,
            type = type,
            setIndex = setIndex,
            title = title,
            isCompleted = completed,
            isUnlocked = unlocked,
        )
    }
}

private fun jlptForLevel(level: String) = when (level) {
    "beginner" -> "N5"
    "intermediate" -> "N4"
    "advanced" -> "N3"
    "master" -> "N2"
    else -> "N5"
}

private fun levelDisplayName(level: String) = when (level) {
    "beginner" -> "Beginner"
    "intermediate" -> "Intermediate"
    "advanced" -> "Advanced"
    "master" -> "Master"
    else -> level.replaceFirstChar { it.uppercase() }
}
