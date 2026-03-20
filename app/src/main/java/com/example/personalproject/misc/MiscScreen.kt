package com.example.personalproject.misc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.personalproject.ui.components.KotobaTopBar

@Composable
fun MiscScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = "Misc  🔢", onBack = onBack)
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Coming soon\n\nNumbers, counters, time (hours/minutes/days/weeks/months/years), body parts, colors, and Japanese holidays.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
