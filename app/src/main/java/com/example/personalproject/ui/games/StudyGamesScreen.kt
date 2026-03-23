package com.example.personalproject.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.ui.components.KotobaTopBar

private data class GameEntry(
    val gameType: String,
    val icon: String,
    val name: String,
    val description: String,
    val implemented: Boolean = false,
)

private data class StudySet(val key: String, val displayName: String)

private val implementedGames = listOf(
    GameEntry("FLASHCARDS",    "🃏", "Flashcards",    "Study cards\none at a time",            implemented = true),
    GameEntry("TIMED_QUIZ",    "⏱",  "Timed Quiz",    "Multiple choice\nunder pressure",        implemented = true),
    GameEntry("MATCH_PAIRS",   "🔗", "Match Pairs",   "Match word\nto meaning",                 implemented = true),
    GameEntry("KANA_SPEED",    "⚡", "Speed Round",   "Tap hiragana\nbefore time runs out",     implemented = true),
    GameEntry("KANA_SWIPE",    "👆", "Kana Swipe",    "Tap hiragana tiles\nto spell the word",  implemented = true),
    GameEntry("KANJI_DROP",    "🎮", "Kanji Drop",    "Kanji falls—\npick the reading fast",    implemented = true),
    GameEntry("KANJI_BUILDER", "⬛", "Kanji Builder", "Tap the radicals\nthat form the kanji", implemented = true),
)

private val comingSoonGames = listOf(
    GameEntry("STROKE_ORDER", "✏️", "Stroke Order", "Write kanji in\ncorrect order"),
    GameEntry("FILL_IN",      "📝", "Fill-in",      "Complete the\nsentence"),
)

@Composable
fun StudyGamesScreen(onGameStart: (gameType: String, setKey: String) -> Unit = { _, _ -> }) {
    val container = LocalAppContainer.current

    var hasSavedVocab by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        hasSavedVocab = container.savedRepository.getSavedItemIds("vocabulary").isNotEmpty()
    }

    val availableSets = remember(hasSavedVocab) {
        buildList {
            add(StudySet("beginner_vocab_0", "Vocabulary 1 (Beginner)"))
            if (hasSavedVocab) add(StudySet("saved_vocabulary", "Saved Vocabulary"))
        }
    }

    var selectedSet by remember { mutableStateOf(availableSets.first()) }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = "Study Games")

        // ── Set selector ──────────────────────────────────────────────────────
        Text(
            text = "Study set",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(availableSets) { set ->
                FilterChip(
                    selected = selectedSet.key == set.key,
                    onClick = { selectedSet = set },
                    label = { Text(set.displayName) },
                )
            }
        }

        Text(
            text = "Games",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(implementedGames) { game ->
                GameCard(game = game, onClick = { onGameStart(game.gameType, selectedSet.key) })
            }
            items(comingSoonGames) { game ->
                GameCard(game = game, onClick = {})
            }
        }
    }
}

@Composable
private fun GameCard(game: GameEntry, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (game.implemented) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
            )
            .clickable(enabled = game.implemented, onClick = onClick)
            .padding(16.dp),
    ) {
        Column {
            Text(text = game.icon, fontSize = 28.sp)
            Text(
                text = game.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (game.implemented)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = game.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (game.implemented)
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
        if (!game.implemented) {
            Text(
                text = "Soon",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}
