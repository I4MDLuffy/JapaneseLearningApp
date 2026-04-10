package com.example.personalproject.ui.basiccharacters

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
fun BasicCharactersScreen(
    onBack: () -> Unit,
    onHiragana: () -> Unit,
    onKatakana: () -> Unit,
) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("basic_characters")) {
            container.onboardingRepository.markScreenSeen("basic_characters")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Basic Characters",
            description = "Japanese uses two phonetic alphabets — Hiragana and Katakana — each with 46 base characters.\n\n" +
                "• Hiragana (ひらがな) — used for native Japanese words and grammatical endings\n" +
                "• Katakana (カタカナ) — used for foreign loanwords, emphasis, and onomatopoeia\n\n" +
                "Tap a card to open its character table. From there you can play a matching game or typing quiz for individual groups, or study all characters at once.",
            onDismiss = { showHelp = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Basic Characters",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "文字",
                fontSize = 56.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The two Japanese phonetic alphabets.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ScriptCard(
                    modifier = Modifier.weight(1f),
                    japanese = "ひらがな",
                    english = "Hiragana",
                    description = "Used for native\nJapanese words",
                    example = "あいうえお",
                    onClick = onHiragana,
                )
                ScriptCard(
                    modifier = Modifier.weight(1f),
                    japanese = "カタカナ",
                    english = "Katakana",
                    description = "Used for foreign\nwords & emphasis",
                    example = "アイウエオ",
                    onClick = onKatakana,
                )
            }
        }
    }
}

@Composable
private fun ScriptCard(
    modifier: Modifier = Modifier,
    japanese: String,
    english: String,
    description: String,
    example: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onClick() }
            .padding(20.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = japanese,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = english,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = example,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
