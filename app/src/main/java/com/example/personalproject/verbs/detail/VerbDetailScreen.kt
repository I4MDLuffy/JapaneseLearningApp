package com.example.personalproject.verbs.detail

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
import com.example.personalproject.data.model.VerbEntry
import com.example.personalproject.ui.components.ItemNavigationBar
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.components.SpeakableText
import com.example.personalproject.util.containsKana
import com.example.personalproject.util.kanaToRomaji
import com.example.personalproject.util.rememberTts
import com.example.personalproject.util.swipeToNavigate
import com.example.personalproject.verbs.detail.mvi.VerbDetailViewModel

@Composable
fun VerbDetailScreen(
    verbId: String,
    onBack: () -> Unit,
    onKanjiClick: ((String) -> Unit)? = null,
    onGrammarClick: ((String) -> Unit)? = null,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
) {
    val container = LocalAppContainer.current
    val settings = LocalAppSettings.current
    val vm: VerbDetailViewModel = viewModel(
        key = verbId,
        factory = viewModelFactory {
            initializer { VerbDetailViewModel(container.verbRepository, verbId) }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val isSaved by container.savedRepository.isItemSavedFlow("verb", verbId)
        .collectAsStateWithLifecycle(initialValue = false)
    val isKnown by container.knownRepository.isItemKnownFlow("verb", verbId)
        .collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()
    val speak = rememberTts()

    Scaffold(
        topBar = {
            KotobaTopBar(
                title = state.entry?.meaning ?: "Verb",
                onBack = onBack,
                actions = {
                    state.entry?.let { entry ->
                        IconButton(onClick = { speak(entry.kanji.ifBlank { entry.dictionaryForm }) }) {
                            Icon(Icons.Outlined.VolumeUp, contentDescription = "Pronounce")
                        }
                    }
                    IconButton(onClick = {
                        scope.launch { container.knownRepository.toggle("verb", verbId) }
                    }) {
                        Icon(
                            imageVector = if (isKnown) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (isKnown) "Mark as unknown" else "Mark as known",
                        )
                    }
                    IconButton(onClick = {
                        val entry = state.entry ?: return@IconButton
                        scope.launch {
                            container.savedRepository.toggle(
                                type = "verb",
                                itemId = verbId,
                                title = entry.kanji.ifBlank { entry.dictionaryForm },
                                reading = entry.dictionaryForm,
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
            state.isLoading -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.entry != null -> VerbDetail(
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

            else -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { Text("Verb not found.") }
        }
    }
}

@Composable
private fun VerbDetail(
    entry: VerbEntry,
    speak: (String) -> Unit,
    showFurigana: Boolean,
    showRomaji: Boolean,
    onKanjiClick: ((String) -> Unit)?,
    onGrammarClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val conjGrammarId = when {
        entry.verbType.contains("Ru", ignoreCase = true) ||
        entry.verbType.contains("Ichidan", ignoreCase = true) -> "g020"
        entry.verbType.contains("Irregular", ignoreCase = true) -> "g022"
        else -> "g021"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero
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
                    text = entry.kanji.ifBlank { entry.dictionaryForm },
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
                if (showFurigana && entry.dictionaryForm.isNotBlank() && entry.kanji.isNotBlank()) {
                    SpeakableText(
                        text = entry.dictionaryForm,
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
            SectionCard(label = "Info") {
                ConjugationRow("Verb Type", entry.verbType, speak = speak, showRomaji = showRomaji, grammarId = conjGrammarId, onGrammarClick = onGrammarClick)
                if (entry.transitivity.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ConjugationRow("Transitivity", entry.transitivity, speak = speak, showRomaji = showRomaji)
                }
                if (entry.stem.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ConjugationRow("Stem", entry.stem, speak = speak, showRomaji = showRomaji)
                }
            }

            SectionCard(label = "Polite Forms") {
                ConjugationRow("Present +", entry.presentAffirmative, speak = speak, showRomaji = showRomaji, grammarId = conjGrammarId, onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Present –", entry.presentNegative, speak = speak, showRomaji = showRomaji, grammarId = conjGrammarId, onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Past +", entry.pastAffirmative, speak = speak, showRomaji = showRomaji, grammarId = "g034", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Past –", entry.pastNegative, speak = speak, showRomaji = showRomaji, grammarId = "g034", onGrammarClick = onGrammarClick)
            }

            SectionCard(label = "Short & Te-forms") {
                ConjugationRow("Te-form", entry.teFormAffirmative, speak = speak, showRomaji = showRomaji, grammarId = "g047", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Present neg.", entry.presentShortNegative, speak = speak, showRomaji = showRomaji, grammarId = "g065", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Te-form (ないで)", entry.teFormNegativeNaide, speak = speak, showRomaji = showRomaji, grammarId = "g072", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Te-form (なくて)", entry.teFormNegativeNakute, speak = speak, showRomaji = showRomaji, grammarId = "g047", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Past short +", entry.pastShortAffirmative, speak = speak, showRomaji = showRomaji, grammarId = "g077", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Past short –", entry.pastShortNegative, speak = speak, showRomaji = showRomaji, grammarId = "g077", onGrammarClick = onGrammarClick)
            }

            SectionCard(label = "Advanced Forms") {
                ConjugationRow("Tai (want to)", entry.tai, speak = speak, showRomaji = showRomaji, grammarId = "g099", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Volitional", entry.volitional, speak = speak, showRomaji = showRomaji, grammarId = "g045", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Ba + ", entry.baFormAffirmative, speak = speak, showRomaji = showRomaji, grammarId = "g121", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Ba – ", entry.baFormNegative, speak = speak, showRomaji = showRomaji, grammarId = "g121", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Potential", entry.potential, speak = speak, showRomaji = showRomaji, grammarId = "g146", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Causative", entry.causative, speak = speak, showRomaji = showRomaji, grammarId = "g143", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Passive", entry.passive, speak = speak, showRomaji = showRomaji, grammarId = "g142", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Caus. passive", entry.causativePassive, speak = speak, showRomaji = showRomaji, grammarId = "g144", onGrammarClick = onGrammarClick)
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
private fun Divider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 6.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

@Composable
private fun ConjugationRow(
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
                .weight(0.4f)
                .then(if (hasLink) Modifier.clickable { onGrammarClick!!(grammarId!!) } else Modifier),
        )
        Column(modifier = Modifier.weight(0.6f)) {
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
