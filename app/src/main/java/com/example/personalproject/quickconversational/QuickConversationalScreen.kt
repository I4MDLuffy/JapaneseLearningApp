package com.example.personalproject.quickconversational

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.components.ScreenHelpDialog

@Composable
fun QuickConversationalScreen(
    onBack: () -> Unit,
    onPhrases: () -> Unit,
) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("conversational")) {
            container.onboardingRepository.markScreenSeen("conversational")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Quick Conversational",
            description = "Quick Conversational gives you practical phrases and vocabulary for everyday situations.\n\n" +
                "Browse phrases grouped by category such as greetings, travel, shopping, and more. Tap any phrase to see its reading and English meaning.\n\n" +
                "These are the expressions you'll use most often when speaking with Japanese people or consuming Japanese media.",
            onDismiss = { showHelp = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Quick Conversational  💬",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showHelp = true }) {
                    Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                }
            },
        )
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Quick-access phrases, common words, kanji, and hiragana/katakana — all referenced from the main lists.\n\nFull layout coming soon.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
