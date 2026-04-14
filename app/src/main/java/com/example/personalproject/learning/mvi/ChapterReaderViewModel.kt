package app.kotori.japanese.learning.mvi

import androidx.lifecycle.viewModelScope
import app.kotori.japanese.data.model.ChapterType
import app.kotori.japanese.data.repository.AdjectiveRepository
import app.kotori.japanese.data.repository.ChapterProgressRepository
import app.kotori.japanese.data.repository.GrammarRepository
import app.kotori.japanese.data.repository.KanjiRepository
import app.kotori.japanese.data.repository.NounRepository
import app.kotori.japanese.data.repository.PhraseRepository
import app.kotori.japanese.data.repository.VerbRepository
import app.kotori.japanese.data.repository.VocabularyRepository
import app.kotori.japanese.mvi.BaseViewModel
import kotlinx.coroutines.launch

private const val TERM_STUDY_CHUNK = 10
private const val VOCAB_CHAPTER_SIZE = 10
private const val KANJI_CHAPTER_SIZE = 5
private const val MC_OPTION_COUNT = 4

/** Count CJK kanji characters (basic block only) in a string. */
private fun String.kanjiCount(): Int = count { it.code in 0x4E00..0x9FFF }

class ChapterReaderViewModel(
    private val level: String,
    private val chapterIndex: Int,
    private val chapterType: ChapterType,
    private val setIndex: Int,
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
            is ChapterReaderAction.PreviousItem -> handlePreviousItem()
            is ChapterReaderAction.RevealStudyVocab -> updateState { copy(isRevealed = true) }
            is ChapterReaderAction.ReviewAgain -> handleReviewAgain()
            is ChapterReaderAction.CompleteChapter -> handleComplete()
            is ChapterReaderAction.SelectMcOption -> handleSelectMcOption(action.option)
            is ChapterReaderAction.ToggleStudyMode -> handleToggleStudyMode()
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
                            .filter { it.lessonNumber == setIndex }
                            .sortedBy { it.difficultyOrder }
                            .map { ChapterItem.GrammarItem(it) }
                    }

                    ChapterType.KANJI -> {
                        kanjiRepository.filterByJlpt(jlpt)
                            .sortedBy { it.id }
                            .chunked(KANJI_CHAPTER_SIZE)
                            .getOrElse(setIndex) { emptyList() }
                            .map { ChapterItem.KanjiItem(it) }
                    }

                    ChapterType.VOCAB -> {
                        val raw = vocabularyRepository.filterByJlpt(jlpt).sortedBy { it.id }
                        val filtered = if (level == "beginner") raw.filter { it.japanese.kanjiCount() <= 1 } else raw
                        filtered.chunked(VOCAB_CHAPTER_SIZE)
                            .getOrElse(setIndex) { emptyList() }
                            .map { ChapterItem.VocabItem(it) }
                    }

                    ChapterType.STUDY_VOCAB -> {
                        val raw = vocabularyRepository.filterByJlpt(jlpt).sortedBy { it.id }
                        val filtered = if (level == "beginner") raw.filter { it.japanese.kanjiCount() <= 1 } else raw
                        filtered.chunked(VOCAB_CHAPTER_SIZE)
                            .getOrElse(setIndex) { emptyList() }
                            .map { ChapterItem.StudyVocabItem(it) }
                    }

                    ChapterType.TERM_STUDY -> {
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
                }

                // ── Rebuild chapter list to determine next chapter ─────────────────
                val rawVocabForNext = vocabularyRepository.filterByJlpt(jlpt).sortedBy { it.id }
                val filteredVocabForNext = if (level == "beginner") rawVocabForNext.filter { it.japanese.kanjiCount() <= 1 } else rawVocabForNext
                val vocabChunksForNext = filteredVocabForNext.chunked(VOCAB_CHAPTER_SIZE)
                val kanjiChunksForNext = kanjiRepository.filterByJlpt(jlpt).sortedBy { it.id }.chunked(KANJI_CHAPTER_SIZE)
                val grammarLessonsForNext = grammarRepository.filterByJlpt(jlpt)
                    .sortedBy { it.lessonNumber }
                    .groupBy { it.lessonNumber }
                    .entries.sortedBy { it.key }
                    .toList()

                data class ChapterSpec(val type: ChapterType, val setIndex: Int, val title: String)
                val allSpecs = buildList {
                    val maxLen = maxOf(vocabChunksForNext.size, grammarLessonsForNext.size, kanjiChunksForNext.size)
                    for (i in 0 until maxLen) {
                        val g = grammarLessonsForNext.getOrNull(i)
                        val v = vocabChunksForNext.getOrNull(i)
                        val k = kanjiChunksForNext.getOrNull(i)
                        if (g != null) add(ChapterSpec(ChapterType.GRAMMAR, g.key, g.value.firstOrNull()?.title ?: "Grammar ${g.key}"))
                        if (k != null) add(ChapterSpec(ChapterType.KANJI, i, "Kanji ${i + 1}"))
                        if (v != null) add(ChapterSpec(ChapterType.VOCAB, i, "Vocabulary ${i + 1}"))
                        if (g != null) add(ChapterSpec(ChapterType.TERM_STUDY, i, "Term Study ${i + 1}"))
                        if (v != null) add(ChapterSpec(ChapterType.STUDY_VOCAB, i, "Study Vocab ${i + 1}"))
                    }
                }
                val next = allSpecs.getOrNull(chapterIndex + 1)

                val initialMcOptions = if (isStudyChapter(chapterType)) buildMcOptions(items, 0) else emptyList()

                updateState {
                    copy(
                        items = items,
                        isLoading = false,
                        nextChapterType = next?.type?.name,
                        nextSetIndex = next?.setIndex,
                        nextChapterTitle = next?.title,
                        mcOptions = initialMcOptions,
                    )
                }
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
            val newOptions = if (isStudyChapter(chapterType)) buildMcOptions(state.items, next) else emptyList()
            updateState { copy(currentIndex = next, isRevealed = false, mcOptions = newOptions, selectedMcOption = null, mcIsCorrect = null) }
        }
    }

    private fun handlePreviousItem() {
        val state = uiState.value
        if (state.currentIndex > 0) {
            val prev = state.currentIndex - 1
            val newOptions = if (isStudyChapter(chapterType)) buildMcOptions(state.items, prev) else emptyList()
            updateState { copy(currentIndex = prev, isRevealed = false, mcOptions = newOptions, selectedMcOption = null, mcIsCorrect = null) }
        }
    }

    private fun handleReviewAgain() {
        val state = uiState.value
        if (state.items.isEmpty()) return
        val mutableItems = state.items.toMutableList()
        val item = mutableItems.removeAt(state.currentIndex)
        val insertRange = (state.currentIndex + 1)..mutableItems.size
        val insertAt = insertRange.random()
        mutableItems.add(insertAt, item)
        val newOptions = if (isStudyChapter(chapterType)) buildMcOptions(mutableItems, state.currentIndex) else emptyList()
        updateState { copy(items = mutableItems, isRevealed = false, mcOptions = newOptions, selectedMcOption = null, mcIsCorrect = null) }
    }

    private fun handleSelectMcOption(option: String) {
        val state = uiState.value
        if (state.selectedMcOption != null) return  // already answered
        val correctAnswer = getCorrectAnswer(state.items.getOrNull(state.currentIndex) ?: return)
        val isCorrect = option == correctAnswer
        updateState { copy(selectedMcOption = option, mcIsCorrect = isCorrect) }
    }

    private fun handleToggleStudyMode() {
        val state = uiState.value
        val newMode = if (state.studyCardMode == StudyCardMode.FLASHCARD) StudyCardMode.MULTIPLE_CHOICE else StudyCardMode.FLASHCARD
        // Rebuild MC options for current item if switching to MC
        val newOptions = if (newMode == StudyCardMode.MULTIPLE_CHOICE && isStudyChapter(chapterType)) {
            buildMcOptions(state.items, state.currentIndex)
        } else emptyList()
        updateState { copy(studyCardMode = newMode, isRevealed = false, mcOptions = newOptions, selectedMcOption = null, mcIsCorrect = null) }
    }

    private fun handleComplete() {
        progressRepository.markCompleted(level, chapterIndex)
        updateState { copy(isCompleted = true) }
    }

    // ── MC helpers ────────────────────────────────────────────────────────────

    private fun isStudyChapter(type: ChapterType) =
        type == ChapterType.STUDY_VOCAB || type == ChapterType.TERM_STUDY || type == ChapterType.KANJI

    /** Extract the "correct answer" (meaning) from a chapter item. */
    private fun getCorrectAnswer(item: ChapterItem): String = when (item) {
        is ChapterItem.StudyVocabItem -> item.word.english
        is ChapterItem.TermStudyItem -> item.meaning
        is ChapterItem.KanjiItem -> item.entry.meaning
        is ChapterItem.VocabItem -> item.word.english
        is ChapterItem.GrammarItem -> item.entry.title
    }

    /**
     * Build 4 answer options: 1 correct + 3 distractors drawn from other items in the chapter.
     * Falls back gracefully when the chapter has fewer than 4 items.
     */
    private fun buildMcOptions(items: List<ChapterItem>, index: Int): List<String> {
        val current = items.getOrNull(index) ?: return emptyList()
        val correct = getCorrectAnswer(current)
        val distractors = items
            .filterIndexed { i, _ -> i != index }
            .map { getCorrectAnswer(it) }
            .filter { it != correct && it.isNotBlank() }
            .shuffled()
            .take(MC_OPTION_COUNT - 1)
        return (distractors + correct).shuffled()
    }
}

private fun jlptForLevel(level: String) = when (level) {
    "beginner" -> "N5"
    "intermediate" -> "N4"
    "advanced" -> "N3"
    "master" -> "N2"
    else -> "N5"
}
