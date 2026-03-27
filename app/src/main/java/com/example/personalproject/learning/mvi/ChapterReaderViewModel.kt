package com.example.personalproject.learning.mvi

import androidx.lifecycle.viewModelScope
import com.example.personalproject.data.model.ChapterType
import com.example.personalproject.data.repository.AdjectiveRepository
import com.example.personalproject.data.repository.ChapterProgressRepository
import com.example.personalproject.data.repository.GrammarRepository
import com.example.personalproject.data.repository.KanjiRepository
import com.example.personalproject.data.repository.NounRepository
import com.example.personalproject.data.repository.PhraseRepository
import com.example.personalproject.data.repository.VerbRepository
import com.example.personalproject.data.repository.VocabularyRepository
import com.example.personalproject.mvi.BaseViewModel
import kotlinx.coroutines.launch

private const val TERM_STUDY_CHUNK = 10

class ChapterReaderViewModel(
    private val level: String,
    private val chapterIndex: Int,
    private val chapterType: ChapterType,
    private val setIndex: Int,         // For GRAMMAR: lessonNumber. For TERM_STUDY: chunk index.
    private val chapterTitle: String,
    private val grammarRepository: GrammarRepository,
    private val vocabularyRepository: VocabularyRepository,
    private val verbRepository: VerbRepository,
    private val adjectiveRepository: AdjectiveRepository,
    private val nounRepository: NounRepository,
    private val phraseRepository: PhraseRepository,
    private val kanjiRepository: KanjiRepository,
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
            is ChapterReaderAction.ReviewAgain -> handleReviewAgain()
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
                        // setIndex = lessonNumber — load all grammar rules for this lesson
                        grammarRepository.filterByJlpt(jlpt)
                            .filter { it.lessonNumber == setIndex }
                            .sortedBy { it.difficultyOrder }
                            .map { ChapterItem.GrammarItem(it) }
                    }

                    ChapterType.TERM_STUDY -> {
                        // setIndex = chunk index (0-based)
                        val vocab = vocabularyRepository.filterByJlpt(jlpt).sortedBy { it.id }
                            .chunked(TERM_STUDY_CHUNK).getOrElse(setIndex) { emptyList() }
                        val verbs = verbRepository.filterByJlpt(jlpt).sortedBy { it.id }
                            .chunked(TERM_STUDY_CHUNK).getOrElse(setIndex) { emptyList() }
                        val adjectives = adjectiveRepository.filterByJlpt(jlpt).sortedBy { it.id }
                            .chunked(TERM_STUDY_CHUNK).getOrElse(setIndex) { emptyList() }
                        val nouns = nounRepository.filterByJlpt(jlpt).sortedBy { it.id }
                            .chunked(TERM_STUDY_CHUNK).getOrElse(setIndex) { emptyList() }
                        val phrases = phraseRepository.filterByJlpt(jlpt).sortedBy { it.id }
                            .chunked(TERM_STUDY_CHUNK).getOrElse(setIndex) { emptyList() }

                        // Collect kanji referenced by vocab/verbs/adjectives in this lesson
                        val kanjiIds = (
                            vocab.flatMap { it.kanjiReferences } +
                            verbs.flatMap { it.kanjiReferences } +
                            adjectives.flatMap { it.kanjiReferences }
                        ).distinct().take(TERM_STUDY_CHUNK)
                        val kanji = kanjiIds.mapNotNull { kanjiRepository.getKanjiById(it) }

                        buildList {
                            addAll(vocab.map { w ->
                                ChapterItem.TermStudyItem("vocab_${w.id}", "vocab", w.japanese, w.hiragana, w.romaji, w.english)
                            })
                            addAll(verbs.map { v ->
                                ChapterItem.TermStudyItem("verb_${v.id}", "verb", v.kanji.ifBlank { v.dictionaryForm }, v.dictionaryForm, v.romaji, v.meaning)
                            })
                            addAll(adjectives.map { a ->
                                ChapterItem.TermStudyItem("adj_${a.id}", "adjective", a.kanji.ifBlank { a.hiragana }, a.hiragana, a.romaji, a.meaning)
                            })
                            addAll(nouns.map { n ->
                                ChapterItem.TermStudyItem("noun_${n.id}", "noun", n.kanji.ifBlank { n.hiragana }, n.hiragana, n.romaji, n.meaning)
                            })
                            addAll(phrases.map { p ->
                                ChapterItem.TermStudyItem("phrase_${p.id}", "phrase", p.phrase, p.reading, p.romaji, p.meaning)
                            })
                            addAll(kanji.map { k ->
                                ChapterItem.TermStudyItem("kanji_${k.id}", "kanji", k.kanji, k.hiragana, k.hiragana, k.meaning)
                            })
                        }
                    }

                    ChapterType.VOCAB -> {
                        vocabularyRepository.filterByJlpt(jlpt)
                            .sortedBy { it.id }
                            .chunked(10)
                            .getOrElse(setIndex) { emptyList() }
                            .map { ChapterItem.VocabItem(it) }
                    }

                    ChapterType.STUDY_VOCAB -> {
                        vocabularyRepository.filterByJlpt(jlpt)
                            .sortedBy { it.id }
                            .chunked(10)
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

    private fun handleReviewAgain() {
        val state = uiState.value
        if (state.items.isEmpty()) return
        val mutableItems = state.items.toMutableList()
        val item = mutableItems.removeAt(state.currentIndex)
        // Insert at a random position after the current one
        val insertRange = (state.currentIndex + 1)..mutableItems.size
        val insertAt = insertRange.random()
        mutableItems.add(insertAt, item)
        updateState { copy(items = mutableItems, isRevealed = false) }
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
