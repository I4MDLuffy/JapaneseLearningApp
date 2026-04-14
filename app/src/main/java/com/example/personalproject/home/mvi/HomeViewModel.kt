package app.kotori.japanese.home.mvi

import app.kotori.japanese.data.repository.ModuleRepository
import app.kotori.japanese.data.repository.VocabularyRepository
import app.kotori.japanese.mvi.BaseViewModel

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
