package com.example.personalproject.home.mvi

import com.example.personalproject.data.repository.ModuleRepository
import com.example.personalproject.data.repository.VocabularyRepository
import com.example.personalproject.mvi.BaseViewModel

class HomeViewModel(
    private val vocabularyRepository: VocabularyRepository,
    private val moduleRepository: ModuleRepository,
) : BaseViewModel<HomeState, HomeAction>(HomeState()) {

    init {
        dispatchAction(HomeAction.LoadData)
    }

    override fun dispatchAction(action: HomeAction) {
        when (action) {
            HomeAction.LoadData -> loadData()
            HomeAction.RefreshWordOfTheDay -> refreshWordOfTheDay()
        }
    }

    private fun loadData() {
        updateState { copy(isLoading = true) }
        execute(
            block = {
                val words = vocabularyRepository.getAllWords()
                val modules = moduleRepository.getAllModules()
                Triple(words, modules, words.randomOrNull())
            },
            onSuccess = { (words, modules, wotd) ->
                updateState {
                    copy(
                        wordOfTheDay = wotd,
                        totalWords = words.size,
                        totalModules = modules.size,
                        isLoading = false,
                    )
                }
            },
            onError = { updateState { copy(isLoading = false) } },
        )
    }

    private fun refreshWordOfTheDay() {
        execute(
            block = { vocabularyRepository.getAllWords().randomOrNull() },
            onSuccess = { word -> updateState { copy(wordOfTheDay = word) } },
        )
    }
}
