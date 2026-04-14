package com.example.personalproject

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import com.example.personalproject.data.db.KotobaDatabase
import com.example.personalproject.data.repository.AdjectiveRepository
import com.example.personalproject.data.repository.ChapterProgressRepository
import com.example.personalproject.data.repository.SavedRepository
import com.example.personalproject.data.repository.DialogueRepository
import com.example.personalproject.data.repository.GrammarRepository
import com.example.personalproject.data.repository.KanaRepository
import com.example.personalproject.data.repository.KanjiRepository
import com.example.personalproject.data.repository.MiscRepository
import com.example.personalproject.data.repository.ModuleRepository
import com.example.personalproject.data.repository.NounRepository
import com.example.personalproject.data.repository.PhraseRepository
import com.example.personalproject.data.repository.RadicalRepository
import com.example.personalproject.data.repository.KanaStatsRepository
import com.example.personalproject.data.repository.KnownRepository
import com.example.personalproject.data.repository.OnboardingRepository
import com.example.personalproject.data.repository.SettingsRepository
import com.example.personalproject.data.repository.VerbRepository
import com.example.personalproject.data.repository.VocabularyRepository

class AppContainer(context: Context) {
    // Legacy
    val vocabularyRepository: VocabularyRepository = VocabularyRepository(context.applicationContext)
    val moduleRepository: ModuleRepository = ModuleRepository()

    // New data types
    val kanjiRepository: KanjiRepository = KanjiRepository(context.applicationContext)
    val verbRepository: VerbRepository = VerbRepository(context.applicationContext)
    val adjectiveRepository: AdjectiveRepository = AdjectiveRepository(context.applicationContext)
    val nounRepository: NounRepository = NounRepository(context.applicationContext)
    val phraseRepository: PhraseRepository = PhraseRepository(context.applicationContext)
    val grammarRepository: GrammarRepository = GrammarRepository(context.applicationContext)
    val kanaRepository: KanaRepository = KanaRepository(context.applicationContext)
    val radicalRepository: RadicalRepository = RadicalRepository(context.applicationContext)
    val dialogueRepository: DialogueRepository = DialogueRepository(context.applicationContext)
    val miscRepository: MiscRepository = MiscRepository(context.applicationContext)

    // Chapter progress (SharedPreferences)
    val chapterProgressRepository: ChapterProgressRepository = ChapterProgressRepository(context.applicationContext)

    // Room database
    private val kotobaDatabase: KotobaDatabase = KotobaDatabase.getInstance(context.applicationContext)

    // Saved items (Room-backed)
    val savedRepository: SavedRepository = SavedRepository.create(kotobaDatabase)

    // Known/mastery items (Room-backed)
    val knownRepository: KnownRepository = KnownRepository.create(kotobaDatabase)

    // Kana weakness tracker (Room-backed)
    val kanaStatsRepository: KanaStatsRepository = KanaStatsRepository.create(kotobaDatabase.kanaStatsDao())

    // Settings (SharedPreferences)
    val settingsRepository: SettingsRepository = SettingsRepository(context.applicationContext)

    // Onboarding / first-visit tracking (SharedPreferences)
    val onboardingRepository: OnboardingRepository = OnboardingRepository(context.applicationContext)
}

val LocalAppContainer = compositionLocalOf<AppContainer> {
    error("No AppContainer provided. Wrap your composable with CompositionLocalProvider(LocalAppContainer provides container).")
}
