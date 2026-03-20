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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalproject.ui.components.KotobaTopBar

private data class GameEntry(
    val number: Int,
    val icon: String,
    val name: String,
    val description: String,
    val available: Boolean = false,
)

private val games = listOf(
    GameEntry(1, "🃏", "Flashcards", "Study cards\none at a time", available = false),
    GameEntry(2, "⏱", "Timed Quiz", "Multiple choice\nunder pressure", available = false),
    GameEntry(3, "⬛", "Kanji Builder", "Assemble kanji\nfrom radicals", available = false),
    GameEntry(4, "👆", "Kana Swipe", "Swipe hiragana\nto form words", available = false),
    GameEntry(5, "🔗", "Match Pairs", "Match kanji\nto meaning", available = false),
    GameEntry(6, "✏️", "Stroke Order", "Write kanji in\ncorrect order", available = false),
    GameEntry(7, "⚡", "Kana Speed", "Kana or romaji —\npick the pair fast", available = false),
    GameEntry(8, "📝", "Fill-in", "Complete the\nsentence", available = false),
    GameEntry(9, "🎮", "Kanji Drop", "Tetris-style\ntyping game", available = false),
)

@Composable
fun StudyGamesScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = "Study Games")

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(games) { game ->
                GameCard(game = game)
            }
        }
    }
}

@Composable
private fun GameCard(game: GameEntry) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (game.available) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
            )
            .clickable(enabled = game.available) { }
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = game.icon,
                fontSize = 28.sp,
            )
            Text(
                text = game.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (game.available)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = game.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (game.available)
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
        if (!game.available) {
            Text(
                text = "Soon",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}
