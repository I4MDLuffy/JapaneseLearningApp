package com.example.personalproject.adjectives.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import com.example.personalproject.adjectives.detail.mvi.AdjectiveDetailViewModel
import com.example.personalproject.data.model.AdjectiveEntry
import com.example.personalproject.ui.components.ItemNavigationBar
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.components.SpeakableText
import com.example.personalproject.util.containsKana
import com.example.personalproject.util.kanaToRomaji
import com.example.personalproject.util.rememberTts
import com.example.personalproject.util.swipeToNavigate

@Composable
fun AdjectiveDetailScreen(
    adjId: String,
    onBack: () -> Unit,
    onKanjiClick: ((String) -> Unit)? = null,
    onGrammarClick: ((String) -> Unit)? = null,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
) {
    val container = LocalAppContainer.current
    val settings = LocalAppSettings.current
    val vm: AdjectiveDetailViewModel = viewModel(
        key = adjId,
        factory = viewModelFactory {
            initializer { AdjectiveDetailViewModel(container.adjectiveRepository, adjId) }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val isSaved by container.savedRepository.isItemSavedFlow("adjective", adjId)
        .collectAsStateWithLifecycle(initialValue = false)
    val isKnown by container.knownRepository.isItemKnownFlow("adjective", adjId)
        .collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()
    val speak = rememberTts()

    Scaffold(
        topBar = {
            KotobaTopBar(
                title = state.entry?.meaning ?: "Adjective",
                onBack = onBack,
                actions = {
                    state.entry?.let { entry ->
                        IconButton(onClick = { speak(entry.kanji.ifBlank { entry.hiragana }) }) {
                            Icon(Icons.Outlined.VolumeUp, contentDescription = "Pronounce")
                        }
                    }
                    IconButton(onClick = {
                        scope.launch { container.knownRepository.toggle("adjective", adjId) }
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
                                type = "adjective",
                                itemId = adjId,
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
            state.entry != null -> AdjDetail(
                entry = state.entry!!,
                speak = speak,
                showFurigana = settings.showFurigana,
                showRomaji = settings.showRomaji,
                onKanjiClick = onKanjiClick,
                onGrammarClick = onGrammarClick,
                modifier = Modifier
                    .padding(padding)
                    .swipeToNavigate(onSwipeLeft = onNext, onSwipeRight = onPrevious),
            )
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Adjective not found.")
            }
        }
    }
}

@Composable
private fun AdjDetail(
    entry: AdjectiveEntry,
    speak: (String) -> Unit,
    showFurigana: Boolean,
    showRomaji: Boolean,
    onKanjiClick: ((String) -> Unit)?,
    onGrammarClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val isIAdj = entry.adjType.equals("i", ignoreCase = true)
    val presentGrammarId = if (isIAdj) "g039" else "g040"
    val pastGrammarId    = if (isIAdj) "g041" else "g042"
    val shortGrammarId   = if (isIAdj) "g078" else "g079"

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
            SectionCard(label = "Present") {
                ConjRow("Affirmative", entry.presentAffirmative, speak, showRomaji, presentGrammarId, onGrammarClick)
                Div()
                ConjRow("Negative", entry.presentNegative, speak, showRomaji, presentGrammarId, onGrammarClick)
                Div()
                ConjRow("Neg. short", entry.presentNegativeShort, speak, showRomaji, shortGrammarId, onGrammarClick)
            }

            SectionCard(label = "Past") {
                ConjRow("Affirmative", entry.pastAffirmative, speak, showRomaji, pastGrammarId, onGrammarClick)
                Div()
                ConjRow("Negative", entry.pastNegative, speak, showRomaji, pastGrammarId, onGrammarClick)
                Div()
                ConjRow("Aff. short", entry.pastAffirmativeShort, speak, showRomaji, shortGrammarId, onGrammarClick)
                Div()
                ConjRow("Neg. short", entry.pastNegativeShort, speak, showRomaji, shortGrammarId, onGrammarClick)
            }

            SectionCard(label = "Te-form & Naru") {
                ConjRow("Te-form +", entry.teFormAffirmative, speak, showRomaji, "g061", onGrammarClick)
                Div()
                ConjRow("Te-form –", entry.teFormNegative, speak, showRomaji, "g061", onGrammarClick)
                if (entry.adjNaru.isNotBlank()) {
                    Div()
                    ConjRow("+ なる", entry.adjNaru, speak, showRomaji, "g094", onGrammarClick)
                }
            }

            if (onKanjiClick != null && entry.kanjiReferences.isNotEmpty()) {
                ReferencesSection("Related Kanji", entry.kanjiReferences, onKanjiClick)
            }
            if (onGrammarClick != null && entry.grammarReferences.isNotEmpty()) {
                ReferencesSection("Related Grammar", entry.grammarReferences, onGrammarClick)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReferencesSection(label: String, ids: List<String>, onClick: (String) -> Unit) {
    SectionCard(label = label) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ids.forEach { id ->
                SuggestionChip(
                    onClick = { onClick(id) },
                    label = { Text(id) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                )
            }
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
private fun ConjRow(
    label: String,
    value: String,
    speak: (String) -> Unit,
    showRomaji: Boolean,
    grammarId: String? = null,
    onGrammarClick: ((String) -> Unit)? = null,
) {
    if (value.isBlank()) return
    val hasLink = grammarId != null && onGrammarClick != null
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (hasLink) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(0.45f)
                .then(if (hasLink) Modifier.clickable { onGrammarClick!!(grammarId!!) } else Modifier),
        )
        Column(modifier = Modifier.weight(0.55f)) {
            SpeakableText(text = value, speak = speak, style = MaterialTheme.typography.bodyMedium)
            if (showRomaji && containsKana(value)) {
                Text(
                    text = kanaToRomaji(value),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
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
