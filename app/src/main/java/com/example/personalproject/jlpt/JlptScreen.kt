package com.example.personalproject.jlpt

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.ui.components.ScreenHelpDialog

private data class JlptLevelInfo(
    val level: String,
    val emoji: String,
    val subtitle: String,
    val color: androidx.compose.ui.graphics.Color,
)

@Composable
fun JlptScreen(onLevelClick: (String) -> Unit) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("jlpt")) {
            container.onboardingRepository.markScreenSeen("jlpt")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "JLPT Study",
            description = "Prepare for the Japanese Language Proficiency Test (JLPT).\n\n" +
                "• N5 — Beginner: basic words, hiragana/katakana, simple sentences\n" +
                "• N4 — Elementary: common words, basic grammar, simple conversations\n" +
                "• N3 — Intermediate: everyday topics, complex sentences\n" +
                "• N2 — Upper-Intermediate: news language, abstract topics\n" +
                "• N1 — Advanced: near-native comprehension\n\n" +
                "Each level shows vocabulary, kanji, and grammar filtered to that level. " +
                "Use Practice Test to simulate a timed JLPT quiz from the app's content.",
            onDismiss = { showHelp = false },
        )
    }

    // Load counts per level
    val vocabCounts by produceState(mapOf<String, Int>()) {
        val all = container.vocabularyRepository.getAllWords()
        value = all.groupBy { it.jlptLevel }.mapValues { it.value.size }
    }
    val kanjiCounts by produceState(mapOf<String, Int>()) {
        val all = container.kanjiRepository.getAllKanji()
        value = all.groupBy { it.jlptLevel }.mapValues { it.value.size }
    }
    val grammarCounts by produceState(mapOf<String, Int>()) {
        val all = container.grammarRepository.getAllGrammar()
        value = all.groupBy { it.jlptLevel }.mapValues { it.value.size }
    }

    val levels = listOf(
        JlptLevelInfo("N5", "🌱", "Beginner · Basic survival Japanese", MaterialTheme.colorScheme.primary),
        JlptLevelInfo("N4", "🌿", "Elementary · Everyday situations", MaterialTheme.colorScheme.secondary),
        JlptLevelInfo("N3", "🌳", "Intermediate · Broad contexts", MaterialTheme.colorScheme.tertiary),
        JlptLevelInfo("N2", "🎋", "Upper-Intermediate · Abstract topics", MaterialTheme.colorScheme.primary),
        JlptLevelInfo("N1", "⛩", "Advanced · Near-native", MaterialTheme.colorScheme.error),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "JLPT Study",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Choose a proficiency level",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
            IconButton(onClick = { showHelp = true }) {
                Icon(
                    Icons.Outlined.HelpOutline,
                    contentDescription = "Help",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            levels.forEach { info ->
                JlptLevelCard(
                    info = info,
                    vocabCount = vocabCounts[info.level] ?: 0,
                    kanjiCount = kanjiCounts[info.level] ?: 0,
                    grammarCount = grammarCounts[info.level] ?: 0,
                    onClick = { onLevelClick(info.level) },
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun JlptLevelCard(
    info: JlptLevelInfo,
    vocabCount: Int,
    kanjiCount: Int,
    grammarCount: Int,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(text = info.emoji, fontSize = 28.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = info.level,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = info.color,
                    )
                    Text(
                        text = info.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }

            if (vocabCount > 0 || kanjiCount > 0 || grammarCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (vocabCount > 0) StatChip(label = "Vocab", count = vocabCount)
                    if (kanjiCount > 0) StatChip(label = "Kanji", count = kanjiCount)
                    if (grammarCount > 0) StatChip(label = "Grammar", count = grammarCount)
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, count: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
    }
}
