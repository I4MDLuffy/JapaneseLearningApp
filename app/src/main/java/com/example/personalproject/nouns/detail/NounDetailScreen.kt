package com.example.personalproject.nouns.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.LocalAppSettings
import com.example.personalproject.data.model.NounEntry
import com.example.personalproject.nouns.detail.mvi.NounDetailViewModel
import com.example.personalproject.ui.components.ItemNavigationBar
import com.example.personalproject.ui.components.JlptBadge
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.components.SpeakableText
import com.example.personalproject.util.rememberTts
import com.example.personalproject.util.swipeToNavigate

@Composable
fun NounDetailScreen(
    nounId: String,
    onBack: () -> Unit,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
) {
    val container = LocalAppContainer.current
    val settings = LocalAppSettings.current
    val vm: NounDetailViewModel = viewModel(
        key = nounId,
        factory = viewModelFactory {
            initializer { NounDetailViewModel(container.nounRepository, nounId) }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val isSaved by container.savedRepository.isItemSavedFlow("noun", nounId)
        .collectAsStateWithLifecycle(initialValue = false)
    val isKnown by container.knownRepository.isItemKnownFlow("noun", nounId)
        .collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()
    val speak = rememberTts()

    Scaffold(
        topBar = {
            KotobaTopBar(
                title = state.entry?.meaning ?: "Noun",
                onBack = onBack,
                actions = {
                    state.entry?.let { entry ->
                        IconButton(onClick = { speak(entry.kanji.ifBlank { entry.hiragana }) }) {
                            Icon(Icons.Outlined.VolumeUp, contentDescription = "Pronounce")
                        }
                    }
                    IconButton(onClick = {
                        scope.launch { container.knownRepository.toggle("noun", nounId) }
                    }) {
                        Icon(
                            imageVector = if (isKnown) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (isKnown) "Mark as unknown" else "Mark as known",
                            tint = if (isKnown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = {
                        val entry = state.entry ?: return@IconButton
                        scope.launch {
                            container.savedRepository.toggle(
                                type = "noun",
                                itemId = nounId,
                                title = entry.kanji.ifBlank { entry.hiragana },
                                reading = entry.hiragana,
                                meaning = entry.meaning,
                            )
                        }
                    }) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSaved) "Unsave" else "Save",
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (onPrevious != null || onNext != null) {
                ItemNavigationBar(onPrevious = onPrevious, onNext = onNext)
            }
        },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.entry != null -> NounDetail(
                entry = state.entry!!,
                speak = speak,
                showFurigana = settings.showFurigana,
                showRomaji = settings.showRomaji,
                modifier = Modifier
                    .padding(padding)
                    .swipeToNavigate(onSwipeLeft = onNext, onSwipeRight = onPrevious),
            )
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Noun not found.")
            }
        }
    }
}

@Composable
private fun NounDetail(
    entry: NounEntry,
    speak: (String) -> Unit,
    showFurigana: Boolean,
    showRomaji: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = entry.kanji.ifBlank { entry.hiragana },
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
                if (showFurigana && entry.hiragana.isNotBlank() && entry.hiragana != entry.kanji) {
                    SpeakableText(
                        text = entry.hiragana,
                        speak = speak,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    )
                }
                if (showRomaji && entry.romaji.isNotBlank()) {
                    Text(
                        text = entry.romaji,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.meaning,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (entry.jlptLevel.isNotBlank()) {
                JlptBadge(level = entry.jlptLevel)
            }

            SectionCard(label = "Details") {
                if (entry.theme.isNotBlank()) {
                    DetailRow("Theme", entry.theme)
                }
                if (entry.pitchAccent.isNotBlank()) {
                    if (entry.theme.isNotBlank()) Div()
                    DetailRow("Pitch accent", entry.pitchAccent)
                }
                if (entry.alternateReading.isNotBlank()) {
                    if (entry.theme.isNotBlank() || entry.pitchAccent.isNotBlank()) Div()
                    DetailRowSpeakable("Alt. reading", entry.alternateReading, speak)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Div() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 6.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.6f))
    }
}

@Composable
private fun DetailRowSpeakable(label: String, value: String, speak: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        SpeakableText(
            text = value,
            speak = speak,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.6f),
        )
    }
}

@Composable
private fun SectionCard(label: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
