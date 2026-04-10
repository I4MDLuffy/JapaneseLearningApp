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
fun PurelyGrammarScreen(onBack: () -> Unit, onGrammarList: () -> Unit) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("grammar")) {
            container.onboardingRepository.markScreenSeen("grammar")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Grammar",
            description = "The Grammar section focuses on Japanese grammar patterns and rules.\n\n" +
                "Browse grammar points organised by JLPT level. Each entry includes a keyword, a plain-language explanation, and an example sentence with its reading and English translation.\n\n" +
                "Understanding grammar patterns helps you construct and decode natural Japanese sentences.",
            onDismiss = { showHelp = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Grammar",
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
                text = "Grammar study\ncoming soon.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
