package app.kotori.japanese

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import app.kotori.japanese.gettingstarted.GettingStartedScreen
import app.kotori.japanese.introduction.IntroductionScreen
import app.kotori.japanese.learning.ChapterReaderScreen
import app.kotori.japanese.learning.LevelScreen
import app.kotori.japanese.misc.CountersScreen
import app.kotori.japanese.misc.DialogueDetailScreen
import app.kotori.japanese.misc.DialogueReadingScreen
import app.kotori.japanese.misc.MiscScreen
import app.kotori.japanese.misc.PurelyGrammarScreen
import app.kotori.japanese.misc.TermStudyScreen
import app.kotori.japanese.modules.detail.ModuleDetailScreen
import app.kotori.japanese.modules.list.ModulesScreen
import app.kotori.japanese.adjectives.detail.AdjectiveDetailScreen
import app.kotori.japanese.adjectives.list.AdjectiveListScreen
import app.kotori.japanese.grammar.detail.GrammarDetailScreen
import app.kotori.japanese.grammar.list.GrammarListScreen
import app.kotori.japanese.kanji.detail.KanjiDetailScreen
import app.kotori.japanese.kanji.list.KanjiListScreen
import app.kotori.japanese.navigation.AdjectiveDetailRoute
import app.kotori.japanese.navigation.AdjectiveListRoute
import app.kotori.japanese.navigation.AdvancedRoute
import app.kotori.japanese.navigation.BasicCharactersRoute
import app.kotori.japanese.navigation.BeginnerRoute
import app.kotori.japanese.navigation.ChapterReaderRoute
import app.kotori.japanese.navigation.StudyGameRoute
import app.kotori.japanese.navigation.CountersRoute
import app.kotori.japanese.navigation.DialogueDetailRoute
import app.kotori.japanese.navigation.DialogueReadingRoute
import app.kotori.japanese.navigation.GettingStartedRoute
import app.kotori.japanese.navigation.GrammarDetailRoute
import app.kotori.japanese.navigation.GrammarListRoute
import app.kotori.japanese.navigation.HiraganaRoute
import app.kotori.japanese.navigation.HomeRoute
import app.kotori.japanese.navigation.IntermediateRoute
import app.kotori.japanese.navigation.KanaGroupGameRoute
import app.kotori.japanese.navigation.KanjiDetailRoute
import app.kotori.japanese.navigation.KanjiListRoute
import app.kotori.japanese.navigation.KatakanaRoute
import app.kotori.japanese.navigation.MasterRoute
import app.kotori.japanese.navigation.MiscRoute
import app.kotori.japanese.navigation.ModuleDetailRoute
import app.kotori.japanese.navigation.ModulesRoute
import app.kotori.japanese.navigation.NavHubRoute
import app.kotori.japanese.navigation.NounDetailRoute
import app.kotori.japanese.navigation.NounListRoute
import app.kotori.japanese.navigation.IntroductionRoute
import app.kotori.japanese.navigation.OpeningRoute
import app.kotori.japanese.navigation.PhraseDetailRoute
import app.kotori.japanese.navigation.PhraseListRoute
import app.kotori.japanese.navigation.PurelyGrammarRoute
import app.kotori.japanese.navigation.QuickConversationalRoute
import app.kotori.japanese.navigation.GameSetupRoute
import app.kotori.japanese.navigation.SavedRoute
import app.kotori.japanese.navigation.SettingsRoute
import app.kotori.japanese.navigation.StudyGamesRoute
import app.kotori.japanese.navigation.TermStudyRoute
import app.kotori.japanese.navigation.VerbDetailRoute
import app.kotori.japanese.navigation.VerbListRoute
import app.kotori.japanese.navigation.VocabularyDetailRoute
import app.kotori.japanese.navigation.VocabularyListRoute
import app.kotori.japanese.nouns.detail.NounDetailScreen
import app.kotori.japanese.nouns.list.NounListScreen
import app.kotori.japanese.phrases.detail.PhraseDetailScreen
import app.kotori.japanese.phrases.list.PhraseListScreen
import app.kotori.japanese.verbs.detail.VerbDetailScreen
import app.kotori.japanese.verbs.list.VerbListScreen
import app.kotori.japanese.navhub.NavHubScreen
import app.kotori.japanese.opening.OpeningScreen
import app.kotori.japanese.quickconversational.QuickConversationalScreen
import app.kotori.japanese.settings.SettingsScreen
import app.kotori.japanese.ui.basiccharacters.BasicCharactersScreen
import app.kotori.japanese.ui.basiccharacters.KanaGroupGameScreen
import app.kotori.japanese.ui.basiccharacters.KanaTableScreen
import app.kotori.japanese.ui.components.BottomNavBar
import app.kotori.japanese.ui.components.KotobaTopBar
import app.kotori.japanese.ui.games.GameSetupScreen
import app.kotori.japanese.ui.games.StudyGameScreen
import app.kotori.japanese.ui.games.StudyGamesScreen
import app.kotori.japanese.ui.home.HomeScreen
import app.kotori.japanese.ui.saved.SavedScreen
import app.kotori.japanese.ui.theme.PersonalProjectTheme
import app.kotori.japanese.data.kana.hiraganaGroups
import app.kotori.japanese.data.kana.katakanaGroups
import app.kotori.japanese.radicals.RadicalDetailScreen
import app.kotori.japanese.radicals.RadicalGameScreen
import app.kotori.japanese.radicals.RadicalKanjiListScreen
import app.kotori.japanese.radicals.RadicalListScreen
import app.kotori.japanese.navigation.RadicalDetailRoute
import app.kotori.japanese.navigation.RadicalGameRoute
import app.kotori.japanese.navigation.RadicalKanjiListRoute
import app.kotori.japanese.navigation.RadicalListRoute
import app.kotori.japanese.navigation.JlptRoute
import app.kotori.japanese.navigation.JlptLevelRoute
import app.kotori.japanese.navigation.JlptPracticeTestRoute
import app.kotori.japanese.navigation.OnomatopoeiaRoute
import app.kotori.japanese.navigation.VerbConjugationSetupRoute
import app.kotori.japanese.navigation.VerbConjugationGameRoute
import app.kotori.japanese.navigation.GrammarFillInRoute
import app.kotori.japanese.jlpt.JlptScreen
import app.kotori.japanese.jlpt.JlptLevelScreen
import app.kotori.japanese.jlpt.JlptPracticeTestScreen
import app.kotori.japanese.misc.OnomatopoeiaScreen
import app.kotori.japanese.ui.games.VerbConjugationSetupScreen
import app.kotori.japanese.ui.games.VerbConjugationGameScreen
import app.kotori.japanese.ui.games.GrammarFillInScreen
import app.kotori.japanese.vocabulary.detail.VocabularyDetailScreen
import app.kotori.japanese.vocabulary.list.VocabularyListScreen
import app.kotori.japanese.navigation.ReviewRoute
import app.kotori.japanese.navigation.ProgressRoute
import app.kotori.japanese.navigation.KanaWritingGameRoute
import app.kotori.japanese.ui.review.ReviewScreen
import app.kotori.japanese.ui.progress.ProgressScreen
import app.kotori.japanese.ui.games.KanaWritingGameScreen

@Composable
fun App(appContainer: AppContainer) {
    val settings by appContainer.settingsRepository.settings.collectAsState()

    PersonalProjectTheme(
            appTheme = settings.theme,
            isDarkMode = settings.isDarkMode,
            largerText = settings.largerText,
            highContrast = settings.highContrast,
        ) {
        CompositionLocalProvider(
            LocalAppContainer provides appContainer,
            LocalAppSettings provides settings,
        ) {
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
                            onReview = { navController.navigate(ReviewRoute) },
                            onProgress = { navController.navigate(ProgressRoute) },
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
                            onPlaySelected = { groupIds ->
                                navController.navigate(KanaGroupGameRoute("hiragana", groupIds))
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
                            onPlaySelected = { groupIds ->
                                navController.navigate(KanaGroupGameRoute("katakana", groupIds))
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
                            onCategoryClick = { category ->
                                navController.navigate(PhraseListRoute(category))
                            },
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
                            onOnomatopoeia = { navController.navigate(OnomatopoeiaRoute) },
                            onKanjiClick = { id -> navController.navigate(KanjiDetailRoute(id)) },
                            onVerbClick = { id -> navController.navigate(VerbDetailRoute(id)) },
                            onAdjectiveClick = { id -> navController.navigate(AdjectiveDetailRoute(id)) },
                            onNounClick = { id -> navController.navigate(NounDetailRoute(id)) },
                            onGrammarClick = { id -> navController.navigate(GrammarDetailRoute(id)) },
                            onPhraseClick = { id -> navController.navigate(PhraseDetailRoute(id)) },
                        )
                    }

                    // ── Radicals ──────────────────────────────────────────────
                    composable<RadicalListRoute> {
                        RadicalListScreen(
                            onBack = { navController.popBackStack() },
                            onRadicalClick = { id, allIds -> navController.navigate(RadicalDetailRoute(id, allIds)) },
                            onStudyGroup = { groupId -> navController.navigate(RadicalGameRoute(groupId)) },
                            onStudyAll = { navController.navigate(RadicalGameRoute("all")) },
                        )
                    }

                    composable<RadicalDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<RadicalDetailRoute>()
                        val ids = route.allIds.split(",").filter { it.isNotBlank() }
                        val idx = ids.indexOf(route.radicalId)
                        RadicalDetailScreen(
                            radicalId = route.radicalId,
                            onBack = { navController.popBackStack() },
                            onViewKanji = { radicalId -> navController.navigate(RadicalKanjiListRoute(radicalId)) },
                            onKanjiClick = { id -> navController.navigate(KanjiDetailRoute(id)) },
                            onPrevious = if (idx > 0) {
                                { navController.navigate(RadicalDetailRoute(ids[idx - 1], route.allIds)) { popUpTo<RadicalDetailRoute> { inclusive = true } } }
                            } else null,
                            onNext = if (idx >= 0 && idx < ids.size - 1) {
                                { navController.navigate(RadicalDetailRoute(ids[idx + 1], route.allIds)) { popUpTo<RadicalDetailRoute> { inclusive = true } } }
                            } else null,
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
                        DialogueReadingScreen(
                            onBack = { navController.popBackStack() },
                            onConversationClick = { title, ids ->
                                navController.navigate(DialogueDetailRoute(title, ids))
                            },
                        )
                    }

                    composable<DialogueDetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<DialogueDetailRoute>()
                        DialogueDetailScreen(
                            conversationTitle = route.conversationTitle,
                            dialogueIds = route.dialogueIds.split(",").filter { it.isNotBlank() },
                            onBack = { navController.popBackStack() },
                        )
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
                            onVocabClick = { id -> navController.navigate(VocabularyDetailRoute(id)) },
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
                    composable<PhraseListRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<PhraseListRoute>()
                        PhraseListScreen(
                            category = route.category,
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
                            },
                            onVerbConjugation = { navController.navigate(VerbConjugationSetupRoute) },
                            onGrammarFillIn = { navController.navigate(GrammarFillInRoute) },
                            onKanaWriting = { navController.navigate(KanaWritingGameRoute("both")) },
                        )
                    }

                    composable<VerbConjugationSetupRoute> {
                        VerbConjugationSetupScreen(
                            onBack = { navController.popBackStack() },
                            onStart = { level, formKeys, count ->
                                navController.navigate(VerbConjugationGameRoute(level, formKeys, count))
                            },
                        )
                    }

                    composable<VerbConjugationGameRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<VerbConjugationGameRoute>()
                        VerbConjugationGameScreen(
                            level = route.level,
                            formKeys = route.formKeys,
                            count = route.count,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable<GrammarFillInRoute> {
                        GrammarFillInScreen(onBack = { navController.popBackStack() })
                    }

                    composable<KanaWritingGameRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<KanaWritingGameRoute>()
                        KanaWritingGameScreen(
                            kanaType = route.kanaType,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    // ── SRS Review ────────────────────────────────────────────
                    composable<ReviewRoute> {
                        ReviewScreen(onBack = { navController.popBackStack() })
                    }

                    // ── Progress dashboard ────────────────────────────────────
                    composable<ProgressRoute> {
                        ProgressScreen(
                            onBack = { navController.popBackStack() },
                            onStartReview = { navController.navigate(ReviewRoute) },
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

                    // ── JLPT ─────────────────────────────────────────────────
                    composable<JlptRoute> {
                        JlptScreen(
                            onLevelClick = { level -> navController.navigate(JlptLevelRoute(level)) },
                        )
                    }

                    composable<JlptLevelRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<JlptLevelRoute>()
                        JlptLevelScreen(
                            level = route.level,
                            onBack = { navController.popBackStack() },
                            onVocab = { navController.navigate(VocabularyListRoute) },
                            onKanji = { navController.navigate(KanjiListRoute) },
                            onGrammar = { navController.navigate(GrammarListRoute) },
                            onPhrases = { navController.navigate(PhraseListRoute()) },
                            onPracticeTest = { level -> navController.navigate(JlptPracticeTestRoute(level)) },
                        )
                    }

                    composable<JlptPracticeTestRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<JlptPracticeTestRoute>()
                        JlptPracticeTestScreen(
                            level = route.level,
                            onBack = { navController.popBackStack() },
                        )
                    }

                    // ── Onomatopoeia ──────────────────────────────────────────
                    composable<OnomatopoeiaRoute> {
                        OnomatopoeiaScreen(onBack = { navController.popBackStack() })
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
