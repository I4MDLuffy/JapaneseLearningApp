package com.example.personalproject.radicals.mvi

import com.example.personalproject.data.model.RadicalEntry
import com.example.personalproject.data.repository.RadicalRepository
import com.example.personalproject.mvi.BaseViewModel
import kotlinx.coroutines.delay

class RadicalGameViewModel(
    private val groupId: String,
    private val radicalRepository: RadicalRepository,
) : BaseViewModel<RadicalGameState, RadicalGameAction>(RadicalGameState()) {

    init {
        loadRadicals()
    }

    // Full pool kept for building distractors when group is small
    private var allRadicals: List<RadicalEntry> = emptyList()

    private fun loadRadicals() {
        execute(
            block = {
                val all = radicalRepository.getAllRadicals()
                val entries = if (groupId == "all") all.shuffled()
                else all.filter { it.strokeCount.toString() == groupId }.shuffled()
                Pair(all, entries)
            },
            onSuccess = { (all, entries) ->
                allRadicals = all
                if (entries.isEmpty()) {
                    updateState { copy(phase = RadicalGamePhase.Results(0, 0)) }
                } else {
                    updateState {
                        copy(
                            phase = RadicalGamePhase.Flashcard(
                                entries = entries,
                                currentIndex = 0,
                                isRevealed = false,
                            )
                        )
                    }
                }
            }
        )
    }

    override fun dispatchAction(action: RadicalGameAction) {
        when (action) {
            is RadicalGameAction.FlipCard -> handleFlip()
            is RadicalGameAction.NextCard -> handleNext()
            is RadicalGameAction.SelectOption -> handleSelect(action.option)
            is RadicalGameAction.SwitchToFlashcard -> handleSwitchToFlashcard()
            is RadicalGameAction.SwitchToMultipleChoice -> handleSwitchToMultipleChoice()
            is RadicalGameAction.Restart -> loadRadicals()
        }
    }

    private fun handleFlip() {
        val phase = state.phase as? RadicalGamePhase.Flashcard ?: return
        updateState { copy(phase = phase.copy(isRevealed = true)) }
    }

    private fun handleNext() {
        val phase = state.phase as? RadicalGamePhase.Flashcard ?: return
        val next = phase.currentIndex + 1
        if (next >= phase.entries.size) {
            updateState { copy(phase = RadicalGamePhase.Results(0, phase.entries.size)) }
        } else {
            updateState { copy(phase = phase.copy(currentIndex = next, isRevealed = false)) }
        }
    }

    private fun handleSelect(option: String) {
        val phase = state.phase as? RadicalGamePhase.MultipleChoice ?: return
        if (phase.selectedOption != null) return
        val correct = phase.entries[phase.currentIndex].meaning
        val newScore = if (option == correct) phase.score + 1 else phase.score
        val updated = phase.copy(selectedOption = option, score = newScore)
        updateState { copy(phase = updated) }

        // Auto-advance after a short delay
        execute(
            block = {
                delay(900L)
                Unit
            },
            onSuccess = {
                val current = state.phase as? RadicalGamePhase.MultipleChoice ?: return@execute
                val next = current.currentIndex + 1
                if (next >= current.entries.size) {
                    updateState { copy(phase = RadicalGamePhase.Results(current.score, current.entries.size)) }
                } else {
                    updateState {
                        copy(
                            phase = current.copy(
                                currentIndex = next,
                                options = buildOptions(current.entries, next),
                                selectedOption = null,
                            )
                        )
                    }
                }
            }
        )
    }

    private fun handleSwitchToFlashcard() {
        val phase = state.phase as? RadicalGamePhase.MultipleChoice ?: return
        updateState {
            copy(
                phase = RadicalGamePhase.Flashcard(
                    entries = phase.entries,
                    currentIndex = phase.currentIndex,
                    isRevealed = false,
                )
            )
        }
    }

    private fun handleSwitchToMultipleChoice() {
        val phase = state.phase as? RadicalGamePhase.Flashcard ?: return
        updateState {
            copy(
                phase = RadicalGamePhase.MultipleChoice(
                    entries = phase.entries,
                    currentIndex = phase.currentIndex,
                    options = buildOptions(phase.entries, phase.currentIndex),
                    selectedOption = null,
                    score = 0,
                )
            )
        }
    }

    private fun buildOptions(entries: List<RadicalEntry>, index: Int): List<String> {
        val correct = entries[index].meaning
        // Use full pool for distractors so small groups still get 4 options
        val pool = if (allRadicals.size >= 4) allRadicals else entries
        val distractors = pool
            .map { it.meaning }
            .filter { it != correct }
            .shuffled()
            .take(3)
        return (distractors + correct).shuffled()
    }

    private val state get() = uiState.value
}
