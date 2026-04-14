package com.example.personalproject.ui.review

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.LocalAppSettings
import com.example.personalproject.ui.components.KotobaTopBar

@Composable
fun ReviewScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val settings = LocalAppSettings.current
    val vm: ReviewViewModel = viewModel(
        factory = viewModelFactory { initializer { ReviewViewModel(container) } }
    )
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = { KotobaTopBar(title = "SRS Review", onBack = onBack) },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when (val s = state) {
                ReviewState.Loading -> CircularProgressIndicator()
                ReviewState.Empty -> EmptyState(onBack = onBack)
                is ReviewState.Reviewing -> ReviewingState(
                    state = s,
                    showFurigana = settings.showFurigana,
                    showRomaji = settings.showRomaji,
                    onTapReveal = { vm.reveal() },
                    onCorrect = { vm.recordResult(true) },
                    onIncorrect = { vm.recordResult(false) },
                )
                is ReviewState.Finished -> FinishedState(
                    state = s,
                    onBack = onBack,
                    onRestart = { vm.restart() },
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(32.dp),
    ) {
        Text("🎉", fontSize = 48.sp)
        Text(
            "All caught up!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "No items are due for review right now.\nMark items as Known (★) to add them to your review queue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}

@Composable
private fun ReviewingState(
    state: ReviewState.Reviewing,
    showFurigana: Boolean,
    showRomaji: Boolean,
    onTapReveal: () -> Unit,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Progress bar
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "${state.currentIndex + 1} / ${state.items.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "✓ ${state.correctCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        "✗ ${state.incorrectCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        // Flashcard
        AnimatedContent(
            targetState = state.current.itemId to state.isRevealed,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
            label = "card",
        ) { (_, revealed) ->
            FlashCard(
                item = state.current,
                isRevealed = revealed,
                showFurigana = showFurigana,
                showRomaji = showRomaji,
                onTap = if (!revealed) onTapReveal else null,
            )
        }

        // Action buttons
        if (state.isRevealed) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onIncorrect,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Again", modifier = Modifier.padding(start = 6.dp))
                }
                Button(
                    onClick = onCorrect,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Got it", modifier = Modifier.padding(start = 6.dp))
                }
            }
        } else {
            Text(
                "Tap the card to reveal the answer",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FlashCard(
    item: ReviewItem,
    isRevealed: Boolean,
    showFurigana: Boolean,
    showRomaji: Boolean,
    onTap: (() -> Unit)?,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(if (onTap != null) Modifier.clickable(onClick = onTap) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            // Type badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = item.type.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Main Japanese form
            Text(
                text = item.title,
                fontSize = if (item.type == "grammar") 24.sp else 64.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            // Furigana reading
            if (showFurigana && item.reading.isNotBlank() && item.reading != item.title) {
                Text(
                    text = item.reading,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                )
            }

            // Romaji
            if (showRomaji && item.extra.isNotBlank()) {
                Text(
                    text = item.extra,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }

            if (isRevealed) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.meaning,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun FinishedState(
    state: ReviewState.Finished,
    onBack: () -> Unit,
    onRestart: () -> Unit,
) {
    val pct = if (state.total > 0) (state.correct * 100 / state.total) else 0
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(32.dp),
    ) {
        Text(
            text = if (pct >= 80) "🎉" else if (pct >= 50) "💪" else "📖",
            fontSize = 48.sp,
        )
        Text(
            "Review complete!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "${state.correct} / ${state.total} correct ($pct%)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) { Text("Done") }
            if (state.total > 0) {
                Button(onClick = onRestart) { Text("Review again") }
            }
        }
    }
}
