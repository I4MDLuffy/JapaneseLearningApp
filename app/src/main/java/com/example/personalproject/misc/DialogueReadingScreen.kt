package com.example.personalproject.misc

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
fun DialogueReadingScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("dialogues")) {
            container.onboardingRepository.markScreenSeen("dialogues")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Dialogues",
            description = "Read short Japanese conversations to practice natural language in context.\n\n" +
                "Each dialogue shows the Japanese text alongside its reading (furigana or romaji) and an English translation. Reading dialogues helps you recognise common patterns and vocabulary as they appear in real speech.",
            onDismiss = { showHelp = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Dialogues",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showHelp = true }) {
                    Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                }
            },
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Dialogue reading\ncoming soon.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
