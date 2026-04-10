package com.example.personalproject.adjectives.list

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
import com.example.personalproject.adjectives.list.mvi.AdjectiveListAction
import com.example.personalproject.adjectives.list.mvi.AdjectiveListViewModel
import com.example.personalproject.data.model.AdjectiveEntry
import com.example.personalproject.ui.components.JlptBadge
import com.example.personalproject.ui.components.KotobaTopBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdjectiveListScreen(onAdjectiveClick: (id: String, allIds: String) -> Unit, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: AdjectiveListViewModel = viewModel(
        factory = viewModelFactory { initializer { AdjectiveListViewModel(container.adjectiveRepository) } }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { KotobaTopBar(title = "Adjectives", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { vm.dispatchAction(AdjectiveListAction.Search(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Kanji, romaji, meaning…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { vm.dispatchAction(AdjectiveListAction.Search("")) }) {
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
                                vm.dispatchAction(AdjectiveListAction.FilterByJlpt(next))
                            },
                            label = { Text(level) },
                        )
                    }
                }
            }

            Text(
                text = "${state.displayedEntries.size} adjectives",
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
                    Text("No adjectives found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.displayedEntries, key = { it.id }) { entry ->
                        AdjectiveListItem(entry = entry, onClick = {
                                onAdjectiveClick(entry.id, state.displayedEntries.joinToString("|") { it.id })
                            })
                    }
                }
            }
        }
    }
}

@Composable
private fun AdjectiveListItem(entry: AdjectiveEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.kanji.ifBlank { entry.hiragana },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
            if (entry.hiragana.isNotBlank() && entry.hiragana != entry.kanji) {
                Text(
                    text = entry.hiragana,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = entry.romaji,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = entry.meaning,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (entry.adjType.isNotBlank()) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("${entry.adjType}-adj", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    )
                }
                if (entry.jlptLevel.isNotBlank()) JlptBadge(level = entry.jlptLevel)
            }
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}
