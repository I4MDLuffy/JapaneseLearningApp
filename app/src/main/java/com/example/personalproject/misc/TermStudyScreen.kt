package com.example.personalproject.misc

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.components.ScreenHelpDialog

@Composable
fun TermStudyScreen(
    onBack: () -> Unit,
    onGrammar: () -> Unit,
    onVocabulary: () -> Unit,
    onVerbs: () -> Unit,
    onAdjectives: () -> Unit,
    onNouns: () -> Unit,
    onKanji: () -> Unit,
    onRadicals: () -> Unit,
) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("term_study")) {
            container.onboardingRepository.markScreenSeen("term_study")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Term Study",
            description = "Browse the full vocabulary database by category.\n\n" +
                "Tap a category card to open a searchable list:\n\n" +
                "• Grammar — patterns and rules\n" +
                "• Vocabulary — general word list\n" +
                "• Verbs — with all conjugation forms\n" +
                "• Adjectives — i-adjectives and na-adjectives\n" +
                "• Nouns — categorised nouns\n" +
                "• Kanji — characters with readings and meanings\n" +
                "• Radicals — kanji building blocks\n\n" +
                "Tap any item in a list to see its full detail page. Bookmark items to add them to your Saved list.",
            onDismiss = { showHelp = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Term Study",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showHelp = true }) {
                    Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Choose a category to study",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )

            val categories = listOf(
                Triple("文法", "Grammar", onGrammar),
                Triple("語彙", "Vocabulary", onVocabulary),
                Triple("動詞", "Verbs", onVerbs),
                Triple("形容詞", "Adjectives", onAdjectives),
                Triple("名詞", "Nouns", onNouns),
                Triple("漢字", "Kanji", onKanji),
                Triple("部首", "Radicals", onRadicals),
            )

            categories.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { (japanese, english, onClick) ->
                        CategoryCard(
                            modifier = Modifier.weight(1f),
                            japanese = japanese,
                            english = english,
                            onClick = onClick,
                        )
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    modifier: Modifier = Modifier,
    japanese: String,
    english: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = japanese,
                fontSize = 22.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = english,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
