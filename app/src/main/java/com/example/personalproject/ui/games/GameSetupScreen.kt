package com.example.personalproject.ui.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.games.mvi.StudyItem

private data class ContentTypeOption(val key: String, val label: String, val emoji: String)
private data class JlptOption(val key: String, val label: String)

private val contentTypes = listOf(
    ContentTypeOption("vocab",     "Vocabulary", "📝"),
    ContentTypeOption("kanji",     "Kanji",      "漢"),
    ContentTypeOption("verb",      "Verbs",      "動"),
    ContentTypeOption("adjective", "Adjectives", "形"),
    ContentTypeOption("noun",      "Nouns",      "名"),
    ContentTypeOption("phrase",    "Phrases",    "💬"),
)

private val jlptLevels = listOf(
    JlptOption("all", "All"),
    JlptOption("N5",  "N5"),
    JlptOption("N4",  "N4"),
    JlptOption("N3",  "N3"),
    JlptOption("N2",  "N2"),
    JlptOption("N1",  "N1"),
)

/** Game types that use the browse content-type + JLPT setup. */
private val browseGameTypes = setOf("FLASHCARDS", "TIMED_QUIZ", "MATCH_PAIRS", "KANA_SPEED", "KANA_SWIPE", "FILL_BLANK")

/** Game types that use kanji only (JLPT filter, no content type chips). */
private val kanjiGameTypes = setOf("KANJI_DROP", "KANJI_BUILDER")

private data class GameMeta(val icon: String, val name: String, val description: String)

private val gameMeta = mapOf(
    "FLASHCARDS"    to GameMeta("🃏", "Flashcards",    "Study cards one at a time — flip to reveal"),
    "TIMED_QUIZ"    to GameMeta("⏱",  "Timed Quiz",    "Multiple choice — pick the meaning before time runs out"),
    "FILL_BLANK"    to GameMeta("✏️", "Fill in Blank", "See a word and type the answer from memory — no options provided"),
    "MATCH_PAIRS"   to GameMeta("🔗", "Match Pairs",   "Match each Japanese word with its English meaning"),
    "KANA_SPEED"    to GameMeta("⚡", "Speed Round",   "Tap hiragana tiles to spell the reading before time runs out"),
    "KANA_SWIPE"    to GameMeta("👆", "Kana Swipe",    "Build a word by tapping hiragana tiles in sequence"),
    "KANJI_DROP"    to GameMeta("🎮", "Kanji Drop",    "A kanji falls — tap the correct reading before it hits the bottom"),
    "KANJI_BUILDER" to GameMeta("⬛", "Kanji Builder", "Tap the radical components that make up the shown kanji"),
)

@Composable
fun GameSetupScreen(
    gameType: String,
    onBack: () -> Unit,
    onStart: (setKey: String) -> Unit,
) {
    val container = LocalAppContainer.current
    val meta = gameMeta[gameType] ?: GameMeta("🎮", gameType, "")
    val isBrowseGame = gameType in browseGameTypes
    val isKanjiGame = gameType in kanjiGameTypes

    // ── Kanji-only game state ──────────────────────────────────────────────────
    var kanjiJlpt by remember { mutableStateOf(jlptLevels[0]) }

    // ── Browse game state ──────────────────────────────────────────────────────
    var selectedType by remember { mutableStateOf(contentTypes[0]) }
    var selectedJlpt by remember { mutableStateOf(jlptLevels[0]) }
    var useSaved by remember { mutableStateOf(false) }
    var hasSaved by remember { mutableStateOf(false) }

    // Item picker state
    var searchQuery by remember { mutableStateOf("") }
    var showFilterRow by remember { mutableStateOf(false) }
    var categoryFilter by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<String>() }

    // Load all items for the current type+JLPT
    val allItems by produceState<List<StudyItem>>(initialValue = emptyList(), selectedType, selectedJlpt) {
        value = emptyList()
        value = loadItemsForType(container, selectedType.key, selectedJlpt.key)
    }

    // Available categories in the current set
    val categories = remember(allItems) { allItems.mapNotNull { it.category }.filter { it.isNotBlank() }.distinct().sorted() }

    // Filtered list shown to user
    val filteredItems = remember(allItems, searchQuery, categoryFilter) {
        allItems.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                item.question.contains(searchQuery, ignoreCase = true) ||
                item.answer.contains(searchQuery, ignoreCase = true) ||
                item.reading.contains(searchQuery, ignoreCase = true)
            val matchesCategory = categoryFilter.isBlank() || item.category == categoryFilter
            matchesSearch && matchesCategory
        }
    }

    LaunchedEffect(Unit) {
        hasSaved = container.savedRepository.getSavedItemIds("vocabulary").isNotEmpty()
    }

    // Reset selection when type/jlpt changes
    LaunchedEffect(selectedType, selectedJlpt) { selectedIds.clear() }

    Scaffold(
        topBar = { KotobaTopBar(title = "Game Setup", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // ── Scrollable header (game info + chips) ──────────────────────────
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Game info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = meta.icon, fontSize = 40.sp)
                        Column {
                            Text(
                                text = meta.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = meta.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            )
                        }
                    }
                }

                // ── Kanji game ─────────────────────────────────────────────────
                if (isKanjiGame) {
                    SetupSection(title = "JLPT Level") {
                        LazyRow(contentPadding = PaddingValues(0.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(jlptLevels) { level ->
                                FilterChip(
                                    selected = kanjiJlpt == level,
                                    onClick = { kanjiJlpt = level },
                                    label = { Text(level.label) },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { onStart(kanjiJlpt.key) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Start ${meta.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                if (!isBrowseGame && !isKanjiGame) {
                    Text(
                        text = "No additional setup required for this game.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                    Button(
                        onClick = { onStart("default") },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Start ${meta.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                // ── Browse game ────────────────────────────────────────────────
                if (isBrowseGame) {
                    // Content type chips
                    SetupSection(title = "Content Type") {
                        LazyRow(contentPadding = PaddingValues(0.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(contentTypes) { type ->
                                FilterChip(
                                    selected = !useSaved && selectedType == type,
                                    onClick = { useSaved = false; selectedType = type },
                                    label = { Text("${type.emoji} ${type.label}") },
                                )
                            }
                            if (hasSaved) {
                                item {
                                    FilterChip(
                                        selected = useSaved,
                                        onClick = { useSaved = true; selectedIds.clear() },
                                        label = { Text("⭐ Saved") },
                                    )
                                }
                            }
                        }
                    }

                    if (!useSaved) {
                        // JLPT filter
                        SetupSection(title = "JLPT Level") {
                            LazyRow(contentPadding = PaddingValues(0.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(jlptLevels) { level ->
                                    FilterChip(
                                        selected = selectedJlpt == level,
                                        onClick = { selectedJlpt = level },
                                        label = { Text(level.label) },
                                    )
                                }
                            }
                        }

                        // Selection summary + start button
                        HorizontalDivider()
                        val selectionLabel = when {
                            selectedIds.isNotEmpty() -> "${selectedIds.size} selected"
                            else -> "${allItems.size} ${selectedType.label.lowercase()} available — or pick below"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = selectionLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier.weight(1f),
                            )
                            if (selectedIds.isNotEmpty()) {
                                TextButton(onClick = { selectedIds.clear() }) { Text("Clear") }
                            }
                        }
                        Button(
                            onClick = {
                                val setKey = if (selectedIds.isNotEmpty()) {
                                    "ids:${selectedType.key}:${selectedIds.joinToString(",")}"
                                } else {
                                    "browse:${selectedType.key}:${selectedJlpt.key}"
                                }
                                onStart(setKey)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = allItems.isNotEmpty() || selectedIds.isNotEmpty(),
                        ) {
                            val label = if (selectedIds.isNotEmpty()) "Start with ${selectedIds.size} items" else "Start with all ${allItems.size}"
                            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Saved mode: just start
                        HorizontalDivider()
                        Button(
                            onClick = { onStart("saved_vocabulary") },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Start ${meta.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Item picker list (browse mode, not saved) ──────────────────────
            if (isBrowseGame && !useSaved) {
                HorizontalDivider()

                // Search bar + filter toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search…") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                    )
                    if (categories.isNotEmpty()) {
                        IconButton(onClick = { showFilterRow = !showFilterRow }) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter by category",
                                tint = if (categoryFilter.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                // Category filter chips
                AnimatedVisibility(visible = showFilterRow && categories.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            FilterChip(
                                selected = categoryFilter.isBlank(),
                                onClick = { categoryFilter = "" },
                                label = { Text("All") },
                            )
                        }
                        items(categories) { cat ->
                            FilterChip(
                                selected = categoryFilter == cat,
                                onClick = { categoryFilter = if (categoryFilter == cat) "" else cat },
                                label = { Text(cat) },
                            )
                        }
                    }
                }

                // Select all / none row
                if (filteredItems.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val allFilteredSelected = filteredItems.all { it.id in selectedIds }
                        TextButton(onClick = {
                            if (allFilteredSelected) {
                                filteredItems.forEach { selectedIds.remove(it.id) }
                            } else {
                                filteredItems.forEach { if (it.id !in selectedIds) selectedIds.add(it.id) }
                            }
                        }) {
                            Text(if (allFilteredSelected) "Deselect all" else "Select all (${filteredItems.size})")
                        }
                    }
                }

                if (allItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("No items match your search.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            val isSelected = item.id in selectedIds
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedIds.remove(item.id) else selectedIds.add(item.id)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.question,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (item.reading.isNotBlank() && item.reading != item.question) {
                                            Text(
                                                text = item.reading,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Text(
                                            text = item.answer,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                        )
                                    }
                                }
                                if (item.jlptLevel.isNotBlank()) {
                                    Text(
                                        text = item.jlptLevel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        content()
    }
}

// ── Data loader ───────────────────────────────────────────────────────────────

private suspend fun loadItemsForType(
    container: com.example.personalproject.AppContainer,
    type: String,
    jlpt: String,
): List<StudyItem> {
    fun <T> List<T>.jlptFilter(jlptOf: (T) -> String) = if (jlpt == "all") this else filter { jlptOf(it) == jlpt }
    return when (type) {
        "vocab" -> container.vocabularyRepository.getAllWords().jlptFilter { it.jlptLevel }
            .map { StudyItem(id = it.id, type = "vocab", question = it.japanese, reading = it.hiragana, romaji = it.romaji, answer = it.english, jlptLevel = it.jlptLevel, category = it.partOfSpeech.ifBlank { null }) }
        "kanji" -> container.kanjiRepository.getAllKanji().jlptFilter { it.jlptLevel }
            .map { StudyItem(id = it.id, type = "kanji", question = it.kanji, reading = it.hiragana, romaji = it.hiragana, answer = it.meaning, jlptLevel = it.jlptLevel, category = it.theme.ifBlank { null }) }
        "verb" -> container.verbRepository.getAllVerbs().jlptFilter { it.jlptLevel }
            .map { StudyItem(id = it.id, type = "verb", question = it.kanji.ifBlank { it.dictionaryForm }, reading = it.dictionaryForm, romaji = it.romaji, answer = it.meaning, jlptLevel = it.jlptLevel, category = it.verbType.ifBlank { null }) }
        "adjective" -> container.adjectiveRepository.getAllAdjectives().jlptFilter { it.jlptLevel }
            .map { StudyItem(id = it.id, type = "adjective", question = it.kanji.ifBlank { it.hiragana }, reading = it.hiragana, romaji = it.romaji, answer = it.meaning, jlptLevel = it.jlptLevel, category = it.adjType.ifBlank { null }) }
        "noun" -> container.nounRepository.getAllNouns().jlptFilter { it.jlptLevel }
            .map { StudyItem(id = it.id, type = "noun", question = it.kanji.ifBlank { it.hiragana }, reading = it.hiragana, romaji = it.romaji, answer = it.meaning, jlptLevel = it.jlptLevel, category = it.theme.ifBlank { null }) }
        "phrase" -> container.phraseRepository.getAllPhrases().jlptFilter { it.jlptLevel }
            .map { StudyItem(id = it.id, type = "phrase", question = it.phrase, reading = it.reading, romaji = it.romaji, answer = it.meaning, jlptLevel = it.jlptLevel, category = it.category.ifBlank { null }) }
        else -> emptyList()
    }
}
