package com.example.personalproject.phrases.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import com.example.personalproject.data.model.PhraseEntry
import com.example.personalproject.phrases.detail.mvi.PhraseDetailViewModel
import com.example.personalproject.ui.components.ItemNavigationBar
import com.example.personalproject.ui.components.JlptBadge
import com.example.personalproject.ui.components.KotobaTopBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhraseDetailScreen(
    phraseId: String,
    onBack: () -> Unit,
    onKanjiClick: ((String) -> Unit)? = null,
    onGrammarClick: ((String) -> Unit)? = null,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
) {
    val container = LocalAppContainer.current
    val vm: PhraseDetailViewModel = viewModel(
        key = phraseId,
        factory = viewModelFactory {
            initializer { PhraseDetailViewModel(container.phraseRepository, phraseId) }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val isSaved by container.savedRepository.isItemSavedFlow("phrase", phraseId)
        .collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            KotobaTopBar(
                title = state.entry?.meaning ?: "Phrase",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        val entry = state.entry ?: return@IconButton
                        scope.launch {
                            container.savedRepository.toggle(
                                type = "phrase",
                                itemId = phraseId,
                                title = entry.phrase,
                                reading = entry.reading,
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
            state.entry != null -> PhraseDetail(
                entry = state.entry!!,
                onKanjiClick = onKanjiClick,
                onGrammarClick = onGrammarClick,
                modifier = Modifier.padding(padding),
            )
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Phrase not found.")
            }
        }
    }
}

@Composable
private fun PhraseDetail(
    entry: PhraseEntry,
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
                    text = entry.phrase,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
                if (entry.reading.isNotBlank() && entry.reading != entry.phrase) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.reading,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.romaji,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = entry.meaning,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (entry.jlptLevel.isNotBlank() || entry.category.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (entry.jlptLevel.isNotBlank()) JlptBadge(level = entry.jlptLevel)
                    if (entry.category.isNotBlank()) {
                        Text(
                            text = entry.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
