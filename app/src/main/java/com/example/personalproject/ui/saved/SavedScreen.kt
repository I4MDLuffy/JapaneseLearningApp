package com.example.personalproject.ui.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.data.db.SavedItemEntity
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.components.ScreenHelpDialog
import kotlinx.coroutines.launch

@Composable
fun SavedScreen(
    onStudyVocab: (setKey: String) -> Unit = {},
    onItemClick: (type: String, id: String) -> Unit = { _, _ -> },
) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("saved")) {
            container.onboardingRepository.markScreenSeen("saved")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Saved",
            description = "Items you bookmark anywhere in the app appear here, grouped by type.\n\n" +
                "• Tap any item to open its full detail page\n" +
                "• Tap the bookmark icon on an item to remove it from your saved list\n" +
                "• Tap Study next to Saved Vocabulary to launch a game with those words\n\n" +
                "Use this screen to review and reinforce terms you want to remember.",
            onDismiss = { showHelp = false },
        )
    }

    val allItems by container.savedRepository.getAllItems()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val byType = allItems.groupBy { it.type }
    val typeOrder = listOf("vocab", "kanji", "grammar", "verb", "adjective", "noun", "phrase")
    val typeLabels = mapOf(
        "vocab" to "Saved Vocabulary",
        "kanji" to "Saved Kanji",
        "grammar" to "Saved Grammar",
        "verb" to "Saved Verbs",
        "adjective" to "Saved Adjectives",
        "noun" to "Saved Nouns",
        "phrase" to "Saved Phrases",
    )

    Scaffold(
        topBar = {
            KotobaTopBar(
                title = "Saved",
                actions = {
                    IconButton(onClick = { showHelp = true }) {
                        Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                    }
                },
            )
        },
    ) { padding ->
        if (allItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Bookmark entries to build your study sets.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            typeOrder.forEach { type ->
                val items = byType[type] ?: return@forEach
                if (items.isEmpty()) return@forEach

                item {
                    SectionHeader(
                        title = typeLabels[type] ?: type,
                        count = items.size,
                        onStudy = if (type == "vocab") {
                            { onStudyVocab("saved_vocab") }
                        } else null,
                    )
                }
                items(items, key = { it.id }) { item ->
                    SavedItemRow(
                        item = item,
                        onClick = { onItemClick(item.type, item.itemId) },
                        onUnsave = {
                            scope.launch {
                                container.savedRepository.toggle(
                                    type = item.type,
                                    itemId = item.itemId,
                                    title = item.title,
                                    reading = item.reading,
                                    meaning = item.meaning,
                                )
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, onStudy: (() -> Unit)?) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "$count item${if (count != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }
            if (onStudy != null) {
                Button(onClick = onStudy) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Study", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun SavedItemRow(item: SavedItemEntity, onClick: () -> Unit, onUnsave: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (item.reading.isNotBlank()) {
                Text(
                    text = item.reading,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
            if (item.meaning.isNotBlank()) {
                Text(
                    text = item.meaning,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }
        }
        IconButton(onClick = onUnsave) {
            Icon(Icons.Default.Bookmark, contentDescription = "Unsave", tint = MaterialTheme.colorScheme.primary)
        }
    }
}
