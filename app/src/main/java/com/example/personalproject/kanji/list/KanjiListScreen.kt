package com.example.personalproject.kanji.list

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
import com.example.personalproject.data.model.KanjiEntry
import com.example.personalproject.kanji.list.mvi.KanjiListAction
import com.example.personalproject.kanji.list.mvi.KanjiListViewModel
import com.example.personalproject.ui.components.JlptBadge
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.util.kanaToRomaji

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KanjiListScreen(onKanjiClick: (id: String, allIds: String) -> Unit, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: KanjiListViewModel = viewModel(
        factory = viewModelFactory { initializer { KanjiListViewModel(container.kanjiRepository) } }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { KotobaTopBar(title = "Kanji", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { vm.dispatchAction(KanjiListAction.Search(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Kanji, meaning, reading…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { vm.dispatchAction(KanjiListAction.Search("")) }) {
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
                                vm.dispatchAction(KanjiListAction.FilterByJlpt(next))
                            },
                            label = { Text(level) },
                        )
                    }
                }
            }

            Text(
                text = "${state.displayedEntries.size} kanji",
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
                    Text("No kanji found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.displayedEntries, key = { it.id }) { entry ->
                        KanjiListItem(entry = entry, onClick = {
                                onKanjiClick(entry.id, state.displayedEntries.joinToString("|") { it.id })
                            })
                    }
                }
            }
        }
    }
}

@Composable
private fun KanjiListItem(entry: KanjiEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = entry.kanji,
            fontSize = 40.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(56.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.meaning,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            if (entry.onYomi.isNotEmpty()) {
                Text(
                    text = "音: ${entry.onYomi.joinToString("、")}  ${entry.onYomi.joinToString(" / ") { kanaToRomaji(it) }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (entry.kunYomi.isNotEmpty()) {
                Text(
                    text = "訓: ${entry.kunYomi.joinToString("、")}  ${entry.kunYomi.joinToString(" / ") { kanaToRomaji(it) }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (entry.jlptLevel.isNotBlank()) {
            JlptBadge(level = entry.jlptLevel)
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}
