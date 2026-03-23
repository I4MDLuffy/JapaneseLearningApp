package com.example.personalproject.learning.mvi

import androidx.lifecycle.viewModelScope
import com.example.personalproject.data.model.Chapter
import com.example.personalproject.data.model.ChapterType
import com.example.personalproject.data.repository.ChapterProgressRepository
import com.example.personalproject.data.repository.GrammarRepository
import com.example.personalproject.mvi.BaseViewModel
import kotlinx.coroutines.launch

class LevelViewModel(
    private val level: String,
    private val grammarRepository: GrammarRepository,
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

                // Group grammar by lessonNumber, sorted ascending
                val grammar = grammarRepository.filterByJlpt(jlpt).sortedBy { it.lessonNumber }
                val grammarByLesson = grammar.groupBy { it.lessonNumber }
                    .entries.sortedBy { it.key }

                val chapters = mutableListOf<Chapter>()
                var chapterIndex = 0

                grammarByLesson.forEachIndexed { lessonIdx, (lessonNumber, _) ->
                    // One grammar chapter per lesson (setIndex = lessonNumber for content lookup)
                    chapters.add(makeChapter(chapterIndex, ChapterType.GRAMMAR, lessonNumber, "Lesson $lessonNumber"))
                    chapterIndex++

                    // Term Study chapter paired with this lesson (setIndex = lessonIdx for chunking)
                    chapters.add(makeChapter(chapterIndex, ChapterType.TERM_STUDY, lessonIdx, "Term Study ${lessonIdx + 1}"))
                    chapterIndex++
                }

                updateState { copy(chapters = chapters, isLoading = false) }
            } catch (e: Exception) {
                updateState { copy(error = e.message ?: "Failed to load chapters", isLoading = false) }
            }
        }
    }

    private fun makeChapter(index: Int, type: ChapterType, setIndex: Int, title: String): Chapter {
        val completed = progressRepository.isCompleted(level, index)
        val unlocked = index == 0 || progressRepository.isCompleted(level, index - 1)
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
