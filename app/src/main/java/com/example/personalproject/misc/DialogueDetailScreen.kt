package app.kotori.japanese.misc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import app.kotori.japanese.util.rememberTts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.data.model.DialogueEntry
import app.kotori.japanese.ui.components.KotobaTopBar

@Composable
fun DialogueDetailScreen(
    conversationTitle: String,
    dialogueIds: List<String>,
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val settings by container.settingsRepository.settings.collectAsState()

    var dialogues by remember { mutableStateOf<List<DialogueEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showTranslation by remember { mutableStateOf(true) }
    val speak = rememberTts()

    LaunchedEffect(dialogueIds) {
        val all = container.dialogueRepository.getAllDialogues()
        dialogues = dialogueIds.mapNotNull { id -> all.find { it.id == id } }
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = conversationTitle,
            onBack = onBack,
            actions = {
                // Toggle English translation
                IconToggleButton(
                    checked = showTranslation,
                    onCheckedChange = { showTranslation = it },
                ) {
                    Icon(
                        Icons.Outlined.Translate,
                        contentDescription = if (showTranslation) "Hide translation" else "Show translation",
                        tint = if (showTranslation)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    )
                }
            },
        )

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                itemsIndexed(dialogues) { index, entry ->
                    DialogueBubble(
                        entry = entry,
                        isLeft = index % 2 == 0,
                        showTranslation = showTranslation,
                        showRomaji = settings.showRomaji,
                        speak = speak,
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

// ── Chat bubble ───────────────────────────────────────────────────────────────

@Composable
private fun DialogueBubble(
    entry: DialogueEntry,
    isLeft: Boolean,
    showTranslation: Boolean,
    showRomaji: Boolean,
    speak: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isLeft) Arrangement.Start else Arrangement.End,
    ) {
        if (!isLeft) Spacer(modifier = Modifier.weight(0.12f))

        Column(
            modifier = Modifier
                .weight(0.88f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isLeft) 4.dp else 16.dp,
                        bottomEnd = if (isLeft) 16.dp else 4.dp,
                    ),
                )
                .background(
                    if (isLeft)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = if (isLeft) Alignment.Start else Alignment.End,
        ) {
            // Japanese text + TTS button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = entry.japaneseContent,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isLeft)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f, fill = false),
                )
                IconButton(
                    onClick = { speak(entry.japaneseContent) },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VolumeUp,
                        contentDescription = "Pronounce",
                        modifier = Modifier.size(16.dp),
                        tint = if (isLeft)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    )
                }
            }

            // Reading (if different from Japanese)
            if (entry.reading.isNotBlank() &&
                entry.reading.trim() != entry.japaneseContent.trim()
            ) {
                Text(
                    text = entry.reading.trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLeft)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                )
            }

            // Romaji
            if (showRomaji && entry.romaji.isNotBlank()) {
                Text(
                    text = entry.romaji,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = if (isLeft)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f),
                )
            }

            // English translation
            if (showTranslation) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = if (isLeft)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                )
                Text(
                    text = entry.englishContent,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = if (isLeft)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f),
                )
            }
        }

        if (isLeft) Spacer(modifier = Modifier.weight(0.12f))
    }
}
