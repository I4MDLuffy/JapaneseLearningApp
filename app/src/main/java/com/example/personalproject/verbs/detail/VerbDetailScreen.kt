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
import com.example.personalproject.data.model.VerbEntry
import com.example.personalproject.ui.components.ItemNavigationBar
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.util.containsKana
import com.example.personalproject.util.kanaToRomaji
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
    val vm: VerbDetailViewModel = viewModel(
        key = verbId,
        factory = viewModelFactory {
            initializer { VerbDetailViewModel(container.verbRepository, verbId) }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val isSaved by container.savedRepository.isItemSavedFlow("verb", verbId)
        .collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            KotobaTopBar(
                title = state.entry?.meaning ?: "Verb",
                onBack = onBack,
                actions = {
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
                onKanjiClick = onKanjiClick,
                onGrammarClick = onGrammarClick,
                modifier = Modifier.padding(padding),
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
                if (entry.dictionaryForm.isNotBlank()) {
                    Text(
                        text = entry.dictionaryForm,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.romaji,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                )
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
            // Info
            SectionCard(label = "Info") {
                ConjugationRow("Verb Type", entry.verbType, grammarId = conjGrammarId, onGrammarClick = onGrammarClick)
                if (entry.transitivity.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ConjugationRow("Transitivity", entry.transitivity)
                }
                if (entry.stem.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ConjugationRow("Stem", entry.stem)
                }
            }

            // Polite (long) forms
            SectionCard(label = "Polite Forms") {
                ConjugationRow("Present +", entry.presentAffirmative, grammarId = conjGrammarId, onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Present –", entry.presentNegative, grammarId = conjGrammarId, onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Past +", entry.pastAffirmative, grammarId = "g034", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Past –", entry.pastNegative, grammarId = "g034", onGrammarClick = onGrammarClick)
            }

            // Te-form & short forms
            SectionCard(label = "Short & Te-forms") {
                ConjugationRow("Te-form", entry.teFormAffirmative, grammarId = "g047", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Present neg.", entry.presentShortNegative, grammarId = "g065", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Te-form (ないで)", entry.teFormNegativeNaide, grammarId = "g072", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Te-form (なくて)", entry.teFormNegativeNakute, grammarId = "g047", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Past short +", entry.pastShortAffirmative, grammarId = "g077", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Past short –", entry.pastShortNegative, grammarId = "g077", onGrammarClick = onGrammarClick)
            }

            // Advanced forms
            SectionCard(label = "Advanced Forms") {
                ConjugationRow("Tai (want to)", entry.tai, grammarId = "g099", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Volitional", entry.volitional, grammarId = "g045", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Ba + ", entry.baFormAffirmative, grammarId = "g121", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Ba – ", entry.baFormNegative, grammarId = "g121", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Potential", entry.potential, grammarId = "g146", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Causative", entry.causative, grammarId = "g143", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Passive", entry.passive, grammarId = "g142", onGrammarClick = onGrammarClick)
                Divider()
                ConjugationRow("Caus. passive", entry.causativePassive, grammarId = "g144", onGrammarClick = onGrammarClick)
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
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
            if (containsKana(value)) {
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
