package app.kotori.japanese.grammar.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.LocalAppSettings
import app.kotori.japanese.data.model.GrammarEntry
import app.kotori.japanese.grammar.detail.mvi.GrammarDetailViewModel
import app.kotori.japanese.ui.components.ItemNavigationBar
import app.kotori.japanese.ui.components.JlptBadge
import app.kotori.japanese.ui.components.KotobaTopBar
import app.kotori.japanese.ui.components.SpeakableText
import app.kotori.japanese.util.containsKana
import app.kotori.japanese.util.kanaToRomaji
import app.kotori.japanese.util.rememberTts
import app.kotori.japanese.util.swipeToNavigate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GrammarDetailScreen(
    grammarId: String,
    onBack: () -> Unit,
    onKanjiClick: ((String) -> Unit)? = null,
    onGrammarClick: ((String) -> Unit)? = null,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
) {
    val container = LocalAppContainer.current
    val settings = LocalAppSettings.current
    val vm: GrammarDetailViewModel = viewModel(
        key = grammarId,
        factory = viewModelFactory {
            initializer { GrammarDetailViewModel(container.grammarRepository, grammarId) }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val isSaved by container.savedRepository.isItemSavedFlow("grammar", grammarId)
        .collectAsStateWithLifecycle(initialValue = false)
    val isKnown by container.knownRepository.isItemKnownFlow("grammar", grammarId)
        .collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()
    val speak = rememberTts()

    Scaffold(
        topBar = {
            KotobaTopBar(
                title = state.entry?.title ?: "Grammar",
                onBack = onBack,
                actions = {
                    state.entry?.let { entry ->
                        if (entry.exampleOne.isNotBlank()) {
                            IconButton(onClick = { speak(entry.exampleOne) }) {
                                Icon(Icons.Outlined.VolumeUp, contentDescription = "Pronounce example")
                            }
                        }
                    }
                    IconButton(onClick = {
                        scope.launch { container.knownRepository.toggle("grammar", grammarId) }
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
                                type = "grammar",
                                itemId = grammarId,
                                title = entry.title,
                                reading = "Lesson ${entry.lessonNumber}",
                                meaning = entry.content.take(80),
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
            state.entry != null -> GrammarDetail(
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
                Text("Grammar lesson not found.")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrammarDetail(
    entry: GrammarEntry,
    speak: (String) -> Unit,
    showFurigana: Boolean,
    showRomaji: Boolean,
    onKanjiClick: ((String) -> Unit)?,
    onGrammarClick: ((String) -> Unit)?,
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
                    text = "Lesson ${entry.lessonNumber}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
                if (entry.jlptLevel.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    JlptBadge(level = entry.jlptLevel)
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (entry.content.isNotBlank()) {
                SectionCard(label = "Explanation") {
                    Text(text = entry.content, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (entry.exampleOne.isNotBlank()) {
                ExampleCard("Example 1", entry.exampleOne, speak, showFurigana, showRomaji)
            }
            if (entry.exampleTwo.isNotBlank()) {
                ExampleCard("Example 2", entry.exampleTwo, speak, showFurigana, showRomaji)
            }

            if (entry.supportingContent.isNotBlank()) {
                SectionCard(label = "Notes") {
                    Text(
                        text = entry.supportingContent,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (onKanjiClick != null && entry.relatedKanjiReferences.isNotEmpty()) {
                ReferencesSection("Related Kanji", entry.relatedKanjiReferences, onKanjiClick)
            }
            if (onGrammarClick != null && entry.relatedGrammarReferences.isNotEmpty()) {
                ReferencesSection("Related Grammar", entry.relatedGrammarReferences, onGrammarClick)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReferencesSection(label: String, ids: List<String>, onClick: (String) -> Unit) {
    SectionCard(label = label) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

private data class ParsedExample(
    val japanese: String,
    val reading: String,
    val romaji: String,
    val english: String,
)

private fun parseExample(raw: String): ParsedExample {
    if (raw.isBlank()) return ParsedExample(raw, "", "", "")
    val parenOpen = raw.indexOf('(')
    val parenClose = if (parenOpen >= 0) raw.indexOf(')', parenOpen) else -1
    if (parenOpen >= 0 && parenClose > parenOpen) {
        val inside = raw.substring(parenOpen + 1, parenClose)
        if (containsKana(inside)) {
            val japanese = raw.substring(0, parenOpen).trim()
            val reading = inside.trim()
            val after = raw.substring(parenClose + 1).trim()
            val english = after.removePrefix("—").removePrefix("–").removePrefix("-").trim()
            return ParsedExample(japanese, reading, kanaToRomaji(reading), english)
        }
    }
    val dash = raw.indexOf('—')
    return if (dash >= 0)
        ParsedExample(raw.substring(0, dash).trim(), "", "", raw.substring(dash + 1).trim())
    else
        ParsedExample(raw, "", "", "")
}

@Composable
private fun ExampleCard(
    label: String,
    raw: String,
    speak: (String) -> Unit,
    showFurigana: Boolean,
    showRomaji: Boolean,
) {
    val parsed = remember(raw) { parseExample(raw) }
    SectionCard(label = label) {
        SpeakableText(
            text = parsed.japanese,
            speak = speak,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        )
        if (showFurigana && parsed.reading.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = parsed.reading,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            )
        }
        if (showRomaji && parsed.romaji.isNotBlank()) {
            Text(
                text = parsed.romaji,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        if (parsed.english.isNotBlank()) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )
            Text(
                text = parsed.english,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
