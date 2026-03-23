package com.example.personalproject.ui.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.ui.components.KotobaTopBar

private data class LevelOption(val key: String, val label: String, val jlpt: String)
private data class JlptOption(val key: String, val label: String)

private val vocabLevels = listOf(
    LevelOption("beginner",     "Beginner",     "N5"),
    LevelOption("intermediate", "Intermediate", "N4"),
    LevelOption("advanced",     "Advanced",     "N3"),
    LevelOption("master",       "Master",       "N2"),
)

private val jlptOptions = listOf(
    JlptOption("all", "All"),
    JlptOption("N5",  "N5"),
    JlptOption("N4",  "N4"),
    JlptOption("N3",  "N3"),
    JlptOption("N2",  "N2"),
)

/** Game types that use vocabulary (need level + set selection). */
private val vocabGameTypes = setOf(
    "FLASHCARDS", "TIMED_QUIZ", "MATCH_PAIRS", "KANA_SPEED", "KANA_SWIPE",
)

/** Game types that use kanji (need JLPT filter). */
private val kanjiGameTypes = setOf("KANJI_DROP", "KANJI_BUILDER")

private data class GameMeta(val icon: String, val name: String, val description: String)

private val gameMeta = mapOf(
    "FLASHCARDS"    to GameMeta("🃏", "Flashcards",    "Study vocabulary cards one at a time"),
    "TIMED_QUIZ"    to GameMeta("⏱",  "Timed Quiz",    "Multiple choice — pick the meaning before time runs out"),
    "MATCH_PAIRS"   to GameMeta("🔗", "Match Pairs",   "Match each Japanese word with its English meaning"),
    "KANA_SPEED"    to GameMeta("⚡", "Speed Round",   "Tap hiragana tiles to spell the reading before time runs out"),
    "KANA_SWIPE"    to GameMeta("👆", "Kana Swipe",    "Build a word by tapping hiragana tiles in sequence"),
    "KANJI_DROP"    to GameMeta("🎮", "Kanji Drop",    "A kanji falls — tap the correct reading before it hits the bottom"),
    "KANJI_BUILDER" to GameMeta("⬛", "Kanji Builder", "Tap the radical components that make up the shown kanji"),
)

@Composable
fun GameSetupScreen(
    gameType: String,
    onBack: () -> Unit,
    onStart: (setKey: String) -> Unit,
) {
    val container = LocalAppContainer.current
    val meta = gameMeta[gameType] ?: GameMeta("🎮", gameType, "")
    val isVocabGame = gameType in vocabGameTypes
    val isKanjiGame = gameType in kanjiGameTypes

    // ── Vocabulary game state ──────────────────────────────────────────────────
    var selectedLevel by remember { mutableStateOf(vocabLevels[0]) }
    var useSaved by remember { mutableStateOf(false) }
    var selectedSetIndex by remember { mutableIntStateOf(0) }
    var setCount by remember { mutableIntStateOf(1) }

    LaunchedEffect(selectedLevel, useSaved) {
        if (!useSaved) {
            val words = container.vocabularyRepository.filterByJlpt(selectedLevel.jlpt)
            setCount = (words.size / 5).coerceAtLeast(1)
        }
        selectedSetIndex = 0
    }

    // ── Kanji game state ───────────────────────────────────────────────────────
    var selectedJlpt by remember { mutableStateOf(jlptOptions[0]) }

    // Saved vocab availability
    var hasSavedVocab by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        hasSavedVocab = container.savedRepository.getSavedItemIds("vocabulary").isNotEmpty()
    }

    Scaffold(
        topBar = { KotobaTopBar(title = "Game Setup", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            // ── Game info card ─────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(text = meta.icon, fontSize = 40.sp)
                    Column {
                        Text(
                            text = meta.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = meta.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            // ── Content selection ──────────────────────────────────────────────

            if (isVocabGame) {
                // Source toggle: Level vs Saved
                SetupSection(title = "Source") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(vocabLevels) { level ->
                            FilterChip(
                                selected = !useSaved && selectedLevel == level,
                                onClick = { useSaved = false; selectedLevel = level },
                                label = { Text(level.label) },
                            )
                        }
                        if (hasSavedVocab) {
                            item {
                                FilterChip(
                                    selected = useSaved,
                                    onClick = { useSaved = true },
                                    label = { Text("Saved") },
                                )
                            }
                        }
                    }
                }

                // Set selector (only when a level is chosen)
                if (!useSaved && setCount > 1) {
                    SetupSection(title = "Set  •  ${selectedLevel.label} (${selectedLevel.jlpt})") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items((0 until setCount).toList()) { idx ->
                                FilterChip(
                                    selected = selectedSetIndex == idx,
                                    onClick = { selectedSetIndex = idx },
                                    label = { Text("Set ${idx + 1}") },
                                )
                            }
                        }
                    }
                }
            }

            if (isKanjiGame) {
                SetupSection(title = "JLPT Level") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(jlptOptions) { opt ->
                            FilterChip(
                                selected = selectedJlpt == opt,
                                onClick = { selectedJlpt = opt },
                                label = { Text(opt.label) },
                            )
                        }
                    }
                }
            }

            if (!isVocabGame && !isKanjiGame) {
                Text(
                    text = "No additional setup required for this game.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Start button ──────────────────────────────────────────────────
            Button(
                onClick = {
                    val setKey = when {
                        isKanjiGame -> selectedJlpt.key
                        useSaved -> "saved_vocabulary"
                        else -> "${selectedLevel.key}_vocab_$selectedSetIndex"
                    }
                    onStart(setKey)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Start ${meta.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SetupSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        content()
    }
}
