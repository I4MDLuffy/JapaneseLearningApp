package app.kotori.japanese

import androidx.compose.runtime.compositionLocalOf
import app.kotori.japanese.data.repository.AdjectiveRepository
import app.kotori.japanese.data.repository.ChapterProgressRepository
import app.kotori.japanese.data.repository.SavedRepository
import app.kotori.japanese.data.repository.DialogueRepository
import app.kotori.japanese.data.repository.GrammarRepository
import app.kotori.japanese.data.repository.KanaRepository
import app.kotori.japanese.data.repository.KanjiRepository
import app.kotori.japanese.data.repository.MiscRepository
import app.kotori.japanese.data.repository.ModuleRepository
import app.kotori.japanese.data.repository.NounRepository
import app.kotori.japanese.data.repository.PhraseRepository
import app.kotori.japanese.data.repository.RadicalRepository
import app.kotori.japanese.data.repository.KanaStatsRepository
import app.kotori.japanese.data.repository.KnownRepository
import app.kotori.japanese.data.repository.OnboardingRepository
import app.kotori.japanese.data.repository.SettingsRepository
import app.kotori.japanese.data.repository.VerbRepository
import app.kotori.japanese.data.repository.VocabularyRepository

class AppContainer {
    // CSV-backed repositories (no Context — use Compose Resources internally)
    val vocabularyRepository: VocabularyRepository = VocabularyRepository()
    val moduleRepository: ModuleRepository = ModuleRepository()
    val kanjiRepository: KanjiRepository = KanjiRepository()
    val verbRepository: VerbRepository = VerbRepository()
    val adjectiveRepository: AdjectiveRepository = AdjectiveRepository()
    val nounRepository: NounRepository = NounRepository()
    val phraseRepository: PhraseRepository = PhraseRepository()
    val grammarRepository: GrammarRepository = GrammarRepository()
    val kanaRepository: KanaRepository = KanaRepository()
    val radicalRepository: RadicalRepository = RadicalRepository()
    val dialogueRepository: DialogueRepository = DialogueRepository()
    val miscRepository: MiscRepository = MiscRepository()

    // Settings-backed repositories (platform-specific storage via createSettings())
    val chapterProgressRepository: ChapterProgressRepository =
        ChapterProgressRepository(createSettings("kotoba_chapter_progress"))
    val settingsRepository: SettingsRepository =
        SettingsRepository(createSettings("kotoba_settings"))
    val onboardingRepository: OnboardingRepository =
        OnboardingRepository(createSettings("kotoba_onboarding"))

    // Room-backed repositories
    private val kotobaDatabase = createDatabase()
    val savedRepository: SavedRepository = SavedRepository.create(kotobaDatabase)
    val knownRepository: KnownRepository = KnownRepository.create(kotobaDatabase)
    val kanaStatsRepository: KanaStatsRepository =
        KanaStatsRepository.create(kotobaDatabase.kanaStatsDao())
}

val LocalAppContainer = compositionLocalOf<AppContainer> {
    error("No AppContainer provided. Wrap your composable with CompositionLocalProvider(LocalAppContainer provides container).")
}
