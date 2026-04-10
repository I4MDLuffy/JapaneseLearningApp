package com.example.personalproject.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.material.icons.outlined.HelpOutline
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.components.ScreenHelpDialog

private data class GameEntry(
    val gameType: String,
    val icon: String,
    val name: String,
    val description: String,
    val implemented: Boolean = false,
)

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
fun StudyGamesScreen(onGameStart: (gameType: String) -> Unit = {}) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("study_games")) {
            container.onboardingRepository.markScreenSeen("study_games")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Study Games",
            description = "Reinforce your learning through interactive games.\n\n" +
                "Tap any game card to start — you will choose your study set inside the game.\n\n" +
                "Available games:\n" +
                "• Flashcards — flip cards to study at your own pace\n" +
                "• Timed Quiz — answer multiple-choice questions under pressure\n" +
                "• Match Pairs — connect words to their meanings\n" +
                "• Speed Round — tap hiragana before the timer runs out\n" +
                "• Kana Swipe — tap tiles to spell a word\n" +
                "• Kanji Drop — identify the reading of a falling kanji\n" +
                "• Kanji Builder — tap the radicals that form a kanji",
            onDismiss = { showHelp = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Study Games",
            actions = {
                IconButton(onClick = { showHelp = true }) {
                    Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                }
            },
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(implementedGames) { game ->
                GameCard(game = game, onClick = { onGameStart(game.gameType) })
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
