package com.example.personalproject.grammar.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.data.model.GrammarEntry
import kotlinx.coroutines.launch
import com.example.personalproject.grammar.list.mvi.GrammarListAction
import com.example.personalproject.grammar.list.mvi.GrammarListViewModel
import com.example.personalproject.ui.components.JlptBadge
import com.example.personalproject.ui.components.KotobaTopBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GrammarListScreen(onGrammarClick: (id: String, allIds: String) -> Unit, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: GrammarListViewModel = viewModel(
        factory = viewModelFactory { initializer { GrammarListViewModel(container.grammarRepository) } }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { KotobaTopBar(title = "Grammar", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { vm.dispatchAction(GrammarListAction.Search(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Title, content, category…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { vm.dispatchAction(GrammarListAction.Search("")) }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )

            if (state.availableJlptLevels.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.availableJlptLevels.forEach { level ->
                        FilterChip(
                            selected = state.selectedJlptLevel == level,
                            onClick = {
                                val next = if (state.selectedJlptLevel == level) null else level
                                vm.dispatchAction(GrammarListAction.FilterByJlpt(next))
                            },
                            label = { Text(level) },
                        )
                    }
                }
            }

            Text(
                text = "${state.displayedEntries.size} lessons",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            HorizontalDivider()

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.displayedEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No grammar found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.displayedEntries, key = { it.id }) { entry ->
                        GrammarListItem(entry = entry, onClick = {
                                onGrammarClick(entry.id, state.displayedEntries.joinToString("|") { it.id })
                            })
                    }
                }
            }
        }
    }
}

@Composable
private fun GrammarListItem(entry: GrammarEntry, onClick: () -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val isKnown by container.knownRepository.isItemKnownFlow("grammar", entry.id)
        .collectAsStateWithLifecycle(initialValue = false)
    val isSaved by container.savedRepository.isItemSavedFlow("grammar", entry.id)
        .collectAsStateWithLifecycle(initialValue = false)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Lesson number circle
        Box(
            modifier = Modifier.width(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${entry.lessonNumber}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            if (entry.category.isNotBlank()) {
                Text(
                    text = entry.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Known (star) toggle
        IconButton(onClick = { scope.launch { container.knownRepository.toggle("grammar", entry.id) } }) {
            Icon(
                imageVector = if (isKnown) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (isKnown) "Mark as unknown" else "Mark as known",
                tint = if (isKnown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Bookmark toggle
        IconButton(onClick = { scope.launch { container.savedRepository.toggle("grammar", entry.id, entry.title, "Lesson ${entry.lessonNumber}", entry.content.take(120)) } }) {
            Icon(
                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = if (isSaved) "Remove bookmark" else "Bookmark",
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            if (entry.jlptLevel.isNotBlank()) JlptBadge(level = entry.jlptLevel)
            if (entry.category.isNotBlank()) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(entry.category, fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    )
                )
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}
