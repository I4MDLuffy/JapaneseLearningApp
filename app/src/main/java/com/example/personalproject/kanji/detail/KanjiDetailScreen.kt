package com.example.personalproject.kanji.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.example.personalproject.data.model.KanjiEntry
import com.example.personalproject.kanji.detail.mvi.KanjiDetailViewModel
import com.example.personalproject.ui.components.ItemNavigationBar
import com.example.personalproject.ui.components.JlptBadge
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.util.kanaToRomaji

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KanjiDetailScreen(
    kanjiId: String,
    onBack: () -> Unit,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
) {
    val container = LocalAppContainer.current
    val vm: KanjiDetailViewModel = viewModel(
        key = kanjiId,
        factory = viewModelFactory {
            initializer { KanjiDetailViewModel(container.kanjiRepository, kanjiId) }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val isSaved by container.savedRepository.isItemSavedFlow("kanji", kanjiId)
        .collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            KotobaTopBar(
                title = state.entry?.meaning ?: "Kanji",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        val entry = state.entry ?: return@IconButton
                        scope.launch {
                            container.savedRepository.toggle(
                                type = "kanji",
                                itemId = kanjiId,
                                title = entry.kanji,
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
            state.isLoading -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.entry != null -> KanjiDetail(
                entry = state.entry!!, modifier = Modifier.padding(padding)
            )

            else -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { Text("Kanji not found.") }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KanjiDetail(entry: KanjiEntry, modifier: Modifier = Modifier) {
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
                    text = entry.kanji,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = entry.meaning,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // Tags
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (entry.jlptLevel.isNotBlank()) JlptBadge(level = entry.jlptLevel)
            if (entry.gradeLevel.isNotBlank()) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("Grade ${entry.gradeLevel}") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                )
            }
            if (entry.strokeCount > 0) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("${entry.strokeCount} strokes") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Readings
            if (entry.onYomi.isNotEmpty() || entry.kunYomi.isNotEmpty()) {
                SectionCard(label = "Readings") {
                    if (entry.onYomi.isNotEmpty()) {
                        ReadingRow(label = "音読み (On'yomi)", values = entry.onYomi)
                    }
                    if (entry.kunYomi.isNotEmpty()) {
                        if (entry.onYomi.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                        ReadingRow(label = "訓読み (Kun'yomi)", values = entry.kunYomi)
                    }
                }
            }

            // Component structure
            if (entry.componentStructure.isNotBlank()) {
                SectionCard(label = "Components") {
                    Text(
                        text = entry.componentStructure,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (entry.hiragana.isNotBlank()) {
                SectionCard(label = "Reading") {
                    Text(text = entry.hiragana, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = kanaToRomaji(entry.hiragana),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReadingRow(label: String, values: List<String>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.45f),
        )
        Column(modifier = Modifier.weight(0.55f)) {
            Text(text = values.joinToString("、"), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = values.joinToString(" / ") { kanaToRomaji(it) },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
