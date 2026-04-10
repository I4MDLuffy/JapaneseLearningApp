package com.example.personalproject

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.personalproject.gettingstarted.GettingStartedScreen
import com.example.personalproject.introduction.IntroductionScreen
import com.example.personalproject.learning.ChapterReaderScreen
import com.example.personalproject.learning.LevelScreen
import com.example.personalproject.misc.CountersScreen
import com.example.personalproject.misc.DialogueReadingScreen
import com.example.personalproject.misc.MiscScreen
import com.example.personalproject.misc.PurelyGrammarScreen
import com.example.personalproject.misc.TermStudyScreen
import com.example.personalproject.modules.detail.ModuleDetailScreen
import com.example.personalproject.modules.list.ModulesScreen
import com.example.personalproject.adjectives.detail.AdjectiveDetailScreen
import com.example.personalproject.adjectives.list.AdjectiveListScreen
import com.example.personalproject.grammar.detail.GrammarDetailScreen
import com.example.personalproject.grammar.list.GrammarListScreen
import com.example.personalproject.kanji.detail.KanjiDetailScreen
import com.example.personalproject.kanji.list.KanjiListScreen
import com.example.personalproject.navigation.AdjectiveDetailRoute
import com.example.personalproject.navigation.AdjectiveListRoute
import com.example.personalproject.navigation.AdvancedRoute
import com.example.personalproject.navigation.BasicCharactersRoute
import com.example.personalproject.navigation.BeginnerRoute
import com.example.personalproject.navigation.ChapterReaderRoute
import com.example.personalproject.navigation.StudyGameRoute
import com.example.personalproject.navigation.CountersRoute
import com.example.personalproject.navigation.DialogueReadingRoute
import com.example.personalproject.navigation.GettingStartedRoute
import com.example.personalproject.navigation.GrammarDetailRoute
import com.example.personalproject.navigation.GrammarListRoute
import com.example.personalproject.navigation.HiraganaRoute
import com.example.personalproject.navigation.HomeRoute
import com.example.personalproject.navigation.IntermediateRoute
import com.example.personalproject.navigation.KanaGroupGameRoute
import com.example.personalproject.navigation.KanjiDetailRoute
import com.example.personalproject.navigation.KanjiListRoute
import com.example.personalproject.navigation.KatakanaRoute
import com.example.personalproject.navigation.MasterRoute
import com.example.personalproject.navigation.MiscRoute
import com.example.personalproject.navigation.ModuleDetailRoute
import com.example.personalproject.navigation.ModulesRoute
import com.example.personalproject.navigation.NavHubRoute
import com.example.personalproject.navigation.NounDetailRoute
import com.example.personalproject.navigation.NounListRoute
import com.example.personalproject.navigation.IntroductionRoute
import com.example.personalproject.navigation.OpeningRoute
import com.example.personalproject.navigation.PhraseDetailRoute
import com.example.personalproject.navigation.PhraseListRoute
import com.example.personalproject.navigation.PurelyGrammarRoute
import com.example.personalproject.navigation.QuickConversationalRoute
import com.example.personalproject.navigation.GameSetupRoute
import com.example.personalproject.navigation.SavedRoute
import com.example.personalproject.navigation.SettingsRoute
import com.example.personalproject.navigation.StudyGamesRoute
import com.example.personalproject.navigation.TermStudyRoute
import com.example.personalproject.navigation.VerbDetailRoute
import com.example.personalproject.navigation.VerbListRoute
import com.example.personalproject.navigation.VocabularyDetailRoute
import com.example.personalproject.navigation.VocabularyListRoute
import com.example.personalproject.nouns.detail.NounDetailScreen
import com.example.personalproject.nouns.list.NounListScreen
import com.example.personalproject.phrases.detail.PhraseDetailScreen
import com.example.personalproject.phrases.list.PhraseListScreen
import com.example.personalproject.verbs.detail.VerbDetailScreen
import com.example.personalproject.verbs.list.VerbListScreen
import com.example.personalproject.navhub.NavHubScreen
import com.example.personalproject.opening.OpeningScreen
import com.example.personalproject.quickconversational.QuickConversationalScreen
import com.example.personalproject.settings.SettingsScreen
import com.example.personalproject.ui.basiccharacters.BasicCharactersScreen
import com.example.personalproject.ui.basiccharacters.KanaGroupGameScreen
import com.example.personalproject.ui.basiccharacters.KanaTableScreen
import com.example.personalproject.ui.components.BottomNavBar
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.games.GameSetupScreen
import com.example.personalproject.ui.games.StudyGameScreen
import com.example.personalproject.ui.games.StudyGamesScreen
import com.example.personalproject.ui.home.HomeScreen
import com.example.personalproject.ui.saved.SavedScreen
import com.example.personalproject.ui.theme.PersonalProjectTheme
import com.example.personalproject.data.kana.hiraganaGroups
import com.example.personalproject.data.kana.katakanaGroups
import com.example.personalproject.radicals.RadicalDetailScreen
import com.example.personalproject.radicals.RadicalGameScreen
import com.example.personalproject.radicals.RadicalKanjiListScreen
import com.example.personalproject.radicals.RadicalListScreen
import com.example.personalproject.navigation.RadicalDetailRoute
import com.example.personalproject.navigation.RadicalGameRoute
import com.example.personalproject.navigation.RadicalKanjiListRoute
import com.example.personalproject.navigation.RadicalListRoute
import com.example.personalproject.vocabulary.detail.VocabularyDetailScreen
import com.example.personalproject.vocabulary.list.VocabularyListScreen

@Composable
fun App(appContainer: AppContainer) {
    val settings by appContainer.settingsRepository.settings.collectAsState()

    PersonalProjectTheme(appTheme = settings.theme) {
        CompositionLocalProvider(LocalAppContainer provides appContainer) {
            val navController = rememberNavController()
            val backStack by navController.currentBackStackEntryAsState()
            val currentDest = backStack?.destination

            val showBottomBar = currentDest?.hasRoute(OpeningRoute::class) != true &&
                currentDest?.hasRoute(IntroductionRoute::class) != true

            Scaffold(
                bottomBar = {
                    if (showBottomBar) BottomNavBar(navController)
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = OpeningRoute,
                    modifier = Modifier.padding(innerPadding),
                ) {

                    // ── Opening ───────────────────────────────────────────────
                    composable<OpeningRoute> {
                        OpeningScreen(
                            onStart = {
                                if (appContainer.onboardingRepository.introSeen) {
                                    navController.navigate(HomeRoute) {
                                        popUpTo(OpeningRoute) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate(IntroductionRoute) {
                                        popUpTo(OpeningRoute) { inclusive = true }
                                    }
                                }
                            },
                        )
                    }

                    // ── Introduction (first launch only) ──────────────────────
                    composable<IntroductionRoute> {
                        IntroductionScreen(
                            onGetStarted = {
                                navController.navigate(HomeRoute) {
                                    popUpTo(IntroductionRoute) { inclusive = true }
                                }
                            },
                        )
                    }

                    // ── Home (new main hub) ────────────────────────────────────
                    composable<HomeRoute> {
                        HomeScreen(
                            onBasicCharacters = { navController.navigate(BasicCharactersRoute) },
                            onBeginner = { navController.navigate(BeginnerRoute) },
                            onIntermediate = { navController.navigate(IntermediateRoute) },
                            onAdvanced = { navController.navigate(AdvancedRoute) },
                            onMaster = { navController.navigate(MasterRoute) },
                            onPurelyGrammar = { navController.navigate(GrammarListRoute) },
                            onQuickConversational = { navController.navigate(QuickConversationalRoute) },
                            onCounters = { navController.navigate(CountersRoute) },
                            onTermStudy = { navController.navigate(TermStudyRoute) },
                            onDialogueReading = { navController.navigate(DialogueReadingRoute) },
                        )
                    }

                    // ── Basic Characters ───────────────────────────────────────
                    composable<BasicCharactersRoute> {
                        BasicCharactersScreen(
                            onBack = { navController.popBackStack() },
                            onHiragana = { navController.navigate(HiraganaRoute) },
                            onKatakana = { navController.navigate(KatakanaRoute) },
                        )
                    }

                    composable<HiraganaRoute> {
                        KanaTableScreen(
                            title = "Hiragana",
                            groups = hiraganaGroups,
                            onBack = { navController.popBackStack() },
                            onPlayGroup = { groupId ->
                                navController.navigate(KanaGroupGameRoute("hiragana", groupId))
                            },
                            onPlayAll = {
                                navController.navigate(KanaGroupGameRoute("hiragana", "all"))
                            },
                        )
                    }

                    composable<KatakanaRoute> {
                        KanaTableScreen(
                            title = "Katakana",
                            groups = katakanaGroups,
                            onBack = { navController.popBackStack() },
                            onPlayGroup = { groupId ->
                                navController.navigate(KanaGroupGameRoute("katakana", groupId))
                            },
                            onPlayAll = {
                                navController.navigate(KanaGroupGameRoute("katakana", "all"))
                            },
                        )
                    }

                    composable<KanaGroupGameRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<KanaGroupGameRoute>()
                        KanaGroupGameScreen(
                            kanaType = route.kanaType,
                            groupId = route.groupId,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    // ── Skill-level sections ───────────────────────────────────
                    composable<BeginnerRoute> {
                        LevelScreen(
                            level = "beginner",
                            onBack = { navController.popBackStack() },
                            onChapter = { lvl, idx, type, sIdx, title ->
                                navController.navigate(ChapterReaderRoute(lvl, idx, type, sIdx, title))
                            },
                        )
                    }

                    composable<IntermediateRoute> {
                        LevelScreen(
                            level = "intermediate",
                            onBack = { navController.popBackStack() },
                            onChapter = { lvl, idx, type, sIdx, title ->
                                navController.navigate(ChapterReaderRoute(lvl, idx, type, sIdx, title))
                            },
                        )
                    }

                    composable<AdvancedRoute> {
                        LevelScreen(
                            level = "advanced",
                            onBack = { navController.popBackStack() },
                            onChapter = { lvl, idx, type, sIdx, title ->
                                navController.navigate(ChapterReaderRoute(lvl, idx, type, sIdx, title))
                            },
                        )
                    }

                    composable<MasterRoute> {
                        LevelScreen(
                            level = "master",
                            onBack = { navController.popBackStack() },
                            onChapter = { lvl, idx, type, sIdx, title ->
                                navController.navigate(ChapterReaderRoute(lvl, idx, type, sIdx, title))
                            },
                        )
                    }

                    composable<ChapterReaderRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<ChapterReaderRoute>()
                        ChapterReaderScreen(
                            level = route.level,
                            chapterIndex = route.chapterIndex,
                            chapterType = route.chapterType,
                            setIndex = route.setIndex,
                            chapterTitle = route.chapterTitle,
                            onBack = { navController.popBackStack() },
                            onContinue = { nextType, nextSIdx, nextTitle ->
                                navController.navigate(
                                    ChapterReaderRoute(
                                        level = route.level,
                                        chapterIndex = route.chapterIndex + 1,
                                        chapterType = nextType,
                                        setIndex = nextSIdx,
                                        chapterTitle = nextTitle,
                                    )
                                ) {
                                    popUpTo<ChapterReaderRoute> { inclusive = true }
                                }
                            },
                        )
                    }

                    // ── Explore / Misc ─────────────────────────────────────────
                    composable<PurelyGrammarRoute> {
                        PurelyGrammarScreen(
                            onBack = { navController.popBackStack() },
                            onGrammarList = { navController.navigate(GrammarListRoute) },
                        )
                    }

                    composable<QuickConversationalRoute> {
                        QuickConversationalScreen(
                            onBack = { navController.popBackStack() },
                            onPhrases = { navController.navigate(PhraseListRoute) },
                        )
                    }

                    composable<CountersRoute> {
                        CountersScreen(onBack = { navController.popBackStack() })
                    }

                    composable<TermStudyRoute> {
                        TermStudyScreen(
                            onBack = { navController.popBackStack() },
                            onGrammar = { navController.navigate(GrammarListRoute) },
                            onVocabulary = { navController.navigate(VocabularyListRoute) },
                            onVerbs = { navController.navigate(VerbListRoute) },
                            onAdjectives = { navController.navigate(AdjectiveListRoute) },
                            onNouns = { navController.navigate(NounListRoute) },
                            onKanji = { navController.navigate(KanjiListRoute) },
                            onRadicals = { navController.navigate(RadicalListRoute) },
                        )
                    }

                    // ── Radicals ──────────────────────────────────────────────
                    composable<RadicalListRoute> {
                        RadicalListScreen(
                            onBack = { navController.popBackStack() },
                            onRadicalClick = { id -> navController.navigate(RadicalDetailRoute(id)) },
                            onStudyGroup = { groupId -> navController.navigate(RadicalGameRoute(groupId)) },
                            onStudyAll = { navController.navigate(RadicalGameRoute("all")) },
                        )
                    }

                    composable<RadicalDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<RadicalDetailRoute>()
                        RadicalDetailScreen(
                            radicalId = route.radicalId,
                            onBack = { navController.popBackStack() },
                            onViewKanji = { radicalId -> navController.navigate(RadicalKanjiListRoute(radicalId)) },
                            onKanjiClick = { id -> navController.navigate(KanjiDetailRoute(id)) },
                        )
                    }

                    composable<RadicalKanjiListRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<RadicalKanjiListRoute>()
                        RadicalKanjiListScreen(
                            radicalId = route.radicalId,
                            onBack = { navController.popBackStack() },
                            onKanjiClick = { id -> navController.navigate(KanjiDetailRoute(id)) },
                        )
                    }

                    composable<RadicalGameRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<RadicalGameRoute>()
                        RadicalGameScreen(
                            groupId = route.groupId,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable<DialogueReadingRoute> {
                        DialogueReadingScreen(onBack = { navController.popBackStack() })
                    }

                    // ── Kanji ─────────────────────────────────────────────────
                    composable<KanjiListRoute> {
                        KanjiListScreen(
                            onKanjiClick = { id, allIds -> navController.navigate(KanjiDetailRoute(id, allIds)) },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable<KanjiDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<KanjiDetailRoute>()
                        val ids = if (route.allIds.isBlank()) emptyList() else route.allIds.split("|")
                        val idx = ids.indexOf(route.kanjiId)
                        KanjiDetailScreen(
                            kanjiId = route.kanjiId,
                            onBack = { navController.popBackStack() },
                            onPrevious = if (idx > 0) {
                                { navController.navigate(KanjiDetailRoute(ids[idx - 1], route.allIds)) { popUpTo<KanjiDetailRoute> { inclusive = true } } }
                            } else null,
                            onNext = if (idx >= 0 && idx < ids.size - 1) {
                                { navController.navigate(KanjiDetailRoute(ids[idx + 1], route.allIds)) { popUpTo<KanjiDetailRoute> { inclusive = true } } }
                            } else null,
                        )
                    }

                    // ── Verbs ─────────────────────────────────────────────────
                    composable<VerbListRoute> {
                        VerbListScreen(
                            onVerbClick = { id, allIds -> navController.navigate(VerbDetailRoute(id, allIds)) },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable<VerbDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<VerbDetailRoute>()
                        val ids = if (route.allIds.isBlank()) emptyList() else route.allIds.split("|")
                        val idx = ids.indexOf(route.verbId)
                        VerbDetailScreen(
                            verbId = route.verbId,
                            onBack = { navController.popBackStack() },
                            onKanjiClick = { id -> navController.navigate(KanjiDetailRoute(id)) },
                            onGrammarClick = { id -> navController.navigate(GrammarDetailRoute(id)) },
                            onPrevious = if (idx > 0) {
                                { navController.navigate(VerbDetailRoute(ids[idx - 1], route.allIds)) { popUpTo<VerbDetailRoute> { inclusive = true } } }
                            } else null,
                            onNext = if (idx >= 0 && idx < ids.size - 1) {
                                { navController.navigate(VerbDetailRoute(ids[idx + 1], route.allIds)) { popUpTo<VerbDetailRoute> { inclusive = true } } }
                            } else null,
                        )
                    }

                    // ── Adjectives ────────────────────────────────────────────
                    composable<AdjectiveListRoute> {
                        AdjectiveListScreen(
                            onAdjectiveClick = { id, allIds -> navController.navigate(AdjectiveDetailRoute(id, allIds)) },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable<AdjectiveDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<AdjectiveDetailRoute>()
                        val ids = if (route.allIds.isBlank()) emptyList() else route.allIds.split("|")
                        val idx = ids.indexOf(route.adjId)
                        AdjectiveDetailScreen(
                            adjId = route.adjId,
                            onBack = { navController.popBackStack() },
                            onKanjiClick = { id -> navController.navigate(KanjiDetailRoute(id)) },
                            onGrammarClick = { id -> navController.navigate(GrammarDetailRoute(id)) },
                            onPrevious = if (idx > 0) {
                                { navController.navigate(AdjectiveDetailRoute(ids[idx - 1], route.allIds)) { popUpTo<AdjectiveDetailRoute> { inclusive = true } } }
                            } else null,
                            onNext = if (idx >= 0 && idx < ids.size - 1) {
                                { navController.navigate(AdjectiveDetailRoute(ids[idx + 1], route.allIds)) { popUpTo<AdjectiveDetailRoute> { inclusive = true } } }
                            } else null,
                        )
                    }

                    // ── Nouns ─────────────────────────────────────────────────
                    composable<NounListRoute> {
                        NounListScreen(
                            onNounClick = { id, allIds -> navController.navigate(NounDetailRoute(id, allIds)) },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable<NounDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<NounDetailRoute>()
                        val ids = if (route.allIds.isBlank()) emptyList() else route.allIds.split("|")
                        val idx = ids.indexOf(route.nounId)
                        NounDetailScreen(
                            nounId = route.nounId,
                            onBack = { navController.popBackStack() },
                            onPrevious = if (idx > 0) {
                                { navController.navigate(NounDetailRoute(ids[idx - 1], route.allIds)) { popUpTo<NounDetailRoute> { inclusive = true } } }
                            } else null,
                            onNext = if (idx >= 0 && idx < ids.size - 1) {
                                { navController.navigate(NounDetailRoute(ids[idx + 1], route.allIds)) { popUpTo<NounDetailRoute> { inclusive = true } } }
                            } else null,
                        )
                    }

                    // ── Grammar ───────────────────────────────────────────────
                    composable<GrammarListRoute> {
                        GrammarListScreen(
                            onGrammarClick = { id, allIds -> navController.navigate(GrammarDetailRoute(id, allIds)) },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable<GrammarDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<GrammarDetailRoute>()
                        val ids = if (route.allIds.isBlank()) emptyList() else route.allIds.split("|")
                        val idx = ids.indexOf(route.grammarId)
                        GrammarDetailScreen(
                            grammarId = route.grammarId,
                            onBack = { navController.popBackStack() },
                            onKanjiClick = { id -> navController.navigate(KanjiDetailRoute(id)) },
                            onGrammarClick = { id -> navController.navigate(GrammarDetailRoute(id)) },
                            onPrevious = if (idx > 0) {
                                { navController.navigate(GrammarDetailRoute(ids[idx - 1], route.allIds)) { popUpTo<GrammarDetailRoute> { inclusive = true } } }
                            } else null,
                            onNext = if (idx >= 0 && idx < ids.size - 1) {
                                { navController.navigate(GrammarDetailRoute(ids[idx + 1], route.allIds)) { popUpTo<GrammarDetailRoute> { inclusive = true } } }
                            } else null,
                        )
                    }

                    // ── Phrases ───────────────────────────────────────────────
                    composable<PhraseListRoute> {
                        PhraseListScreen(
                            onPhraseClick = { id, allIds -> navController.navigate(PhraseDetailRoute(id, allIds)) },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable<PhraseDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<PhraseDetailRoute>()
                        val ids = if (route.allIds.isBlank()) emptyList() else route.allIds.split("|")
                        val idx = ids.indexOf(route.phraseId)
                        PhraseDetailScreen(
                            phraseId = route.phraseId,
                            onBack = { navController.popBackStack() },
                            onKanjiClick = { id -> navController.navigate(KanjiDetailRoute(id)) },
                            onGrammarClick = { id -> navController.navigate(GrammarDetailRoute(id)) },
                            onPrevious = if (idx > 0) {
                                { navController.navigate(PhraseDetailRoute(ids[idx - 1], route.allIds)) { popUpTo<PhraseDetailRoute> { inclusive = true } } }
                            } else null,
                            onNext = if (idx >= 0 && idx < ids.size - 1) {
                                { navController.navigate(PhraseDetailRoute(ids[idx + 1], route.allIds)) { popUpTo<PhraseDetailRoute> { inclusive = true } } }
                            } else null,
                        )
                    }

                    // ── Saved & Games ──────────────────────────────────────────
                    composable<SavedRoute> {
                        SavedScreen(
                            onStudyVocab = { setKey ->
                                navController.navigate(StudyGamesRoute)
                            },
                            onItemClick = { type, id ->
                                when (type) {
                                    "kanji" -> navController.navigate(KanjiDetailRoute(id))
                                    "grammar" -> navController.navigate(GrammarDetailRoute(id))
                                    "verb" -> navController.navigate(VerbDetailRoute(id))
                                    "adjective" -> navController.navigate(AdjectiveDetailRoute(id))
                                    "noun" -> navController.navigate(NounDetailRoute(id))
                                    "phrase" -> navController.navigate(PhraseDetailRoute(id))
                                    else -> navController.navigate(VocabularyDetailRoute(id))
                                }
                            },
                        )
                    }

                    composable<StudyGamesRoute> {
                        StudyGamesScreen(
                            onGameStart = { gameType ->
                                navController.navigate(GameSetupRoute(gameType))
                            }
                        )
                    }

                    composable<GameSetupRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<GameSetupRoute>()
                        GameSetupScreen(
                            gameType = route.gameType,
                            onBack = { navController.popBackStack() },
                            onStart = { setKey ->
                                navController.navigate(StudyGameRoute(route.gameType, setKey))
                            },
                        )
                    }

                    composable<StudyGameRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<StudyGameRoute>()
                        StudyGameScreen(
                            gameType = route.gameType,
                            setKey = route.setKey,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    // ── Settings ──────────────────────────────────────────────
                    composable<SettingsRoute> {
                        SettingsScreen(onBack = { navController.popBackStack() })
                    }

                    // ── Legacy: kept for backward compat ──────────────────────
                    composable<NavHubRoute> {
                        NavHubScreen(
                            onGettingStarted = { navController.navigate(GettingStartedRoute) },
                            onBeginner = { navController.navigate(BeginnerRoute) },
                            onIntermediate = { navController.navigate(IntermediateRoute) },
                            onAdvanced = { navController.navigate(AdvancedRoute) },
                            onQuickConversational = { navController.navigate(QuickConversationalRoute) },
                            onMisc = { navController.navigate(MiscRoute) },
                        )
                    }

                    composable<GettingStartedRoute> {
                        GettingStartedScreen(onBack = { navController.popBackStack() })
                    }

                    composable<MiscRoute> {
                        MiscScreen(onBack = { navController.popBackStack() })
                    }

                    composable<VocabularyListRoute> {
                        VocabularyListScreen(
                            onWordClick = { wordId ->
                                navController.navigate(VocabularyDetailRoute(wordId))
                            },
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable<VocabularyDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<VocabularyDetailRoute>()
                        VocabularyDetailScreen(
                            wordId = route.wordId,
                            onBack = { navController.popBackStack() },
                            onKanjiClick = { id -> navController.navigate(KanjiDetailRoute(id)) },
                        )
                    }

                    composable<ModulesRoute> {
                        ModulesScreen(
                            onModuleClick = { moduleId ->
                                navController.navigate(ModuleDetailRoute(moduleId))
                            },
                        )
                    }

                    composable<ModuleDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<ModuleDetailRoute>()
                        ModuleDetailScreen(
                            moduleId = route.moduleId,
                            onBack = { navController.popBackStack() },
                            onWordClick = { wordId ->
                                navController.navigate(VocabularyDetailRoute(wordId))
                            },
                        )
                    }
                }
            }
        }
    }
}

/** Temporary placeholder for list screens not yet implemented. */
@Composable
private fun StubScreen(title: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = title, onBack = onBack)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$title list screen\ncoming in the next phase.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
