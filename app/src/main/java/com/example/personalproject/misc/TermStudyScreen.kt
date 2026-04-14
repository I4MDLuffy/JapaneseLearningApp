package com.example.personalproject.misc

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.components.ScreenHelpDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Unified search result across all content types
data class SearchResult(
    val type: String,
    val itemId: String,
    val title: String,
    val reading: String,
    val meaning: String,
)

@Composable
fun TermStudyScreen(
    onBack: () -> Unit,
    onGrammar: () -> Unit,
    onVocabulary: () -> Unit,
    onVerbs: () -> Unit,
    onAdjectives: () -> Unit,
    onNouns: () -> Unit,
    onKanji: () -> Unit,
    onRadicals: () -> Unit,
    onOnomatopoeia: () -> Unit,
    // Navigation callbacks for search result taps
    onKanjiClick: ((String) -> Unit)? = null,
    onVerbClick: ((String) -> Unit)? = null,
    onAdjectiveClick: ((String) -> Unit)? = null,
    onNounClick: ((String) -> Unit)? = null,
    onGrammarClick: ((String) -> Unit)? = null,
    onPhraseClick: ((String) -> Unit)? = null,
) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("term_study")) {
            container.onboardingRepository.markScreenSeen("term_study")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Term Study",
            description = "Browse the full vocabulary database by category.\n\n" +
                "Search the box to find items across all categories at once, or tap a category card to open its full list.\n\n" +
                "The progress bar on each card shows how many items in that category you have marked as Known (★).",
            onDismiss = { showHelp = false },
        )
    }

    // Known counts — reactive
    val knownGrammar by container.knownRepository.getKnownCount("grammar").collectAsStateWithLifecycle(0)
    val knownVocab   by container.knownRepository.getKnownCount("vocab").collectAsStateWithLifecycle(0)
    val knownVerb    by container.knownRepository.getKnownCount("verb").collectAsStateWithLifecycle(0)
    val knownAdj     by container.knownRepository.getKnownCount("adjective").collectAsStateWithLifecycle(0)
    val knownNoun    by container.knownRepository.getKnownCount("noun").collectAsStateWithLifecycle(0)
    val knownKanji   by container.knownRepository.getKnownCount("kanji").collectAsStateWithLifecycle(0)
    val knownRadical by container.knownRepository.getKnownCount("radical").collectAsStateWithLifecycle(0)

    // Total counts — loaded once
    val totalGrammar by produceState(0) { value = container.grammarRepository.getAllGrammar().size }
    val totalVocab   by produceState(0) { value = container.vocabularyRepository.getAllWords().size }
    val totalVerb    by produceState(0) { value = container.verbRepository.getAllVerbs().size }
    val totalAdj     by produceState(0) { value = container.adjectiveRepository.getAllAdjectives().size }
    val totalNoun    by produceState(0) { value = container.nounRepository.getAllNouns().size }
    val totalKanji   by produceState(0) { value = container.kanjiRepository.getAllKanji().size }
    val totalRadical by produceState(0) { value = container.radicalRepository.getAllRadicals().size }

    // Global search results — recalculated when query changes
    val searchResults by produceState<List<SearchResult>>(emptyList(), searchQuery) {
        if (searchQuery.isBlank()) { value = emptyList(); return@produceState }
        value = withContext(Dispatchers.Default) {
            val q = searchQuery.trim()
            val results = mutableListOf<SearchResult>()
            results += container.kanjiRepository.search(q).take(5).map {
                SearchResult("kanji", it.id, it.kanji, it.hiragana, it.meaning)
            }
            results += container.verbRepository.search(q).take(5).map {
                SearchResult("verb", it.id, it.kanji.ifBlank { it.dictionaryForm }, it.dictionaryForm, it.meaning)
            }
            results += container.adjectiveRepository.search(q).take(5).map {
                SearchResult("adjective", it.id, it.kanji.ifBlank { it.hiragana }, it.hiragana, it.meaning)
            }
            results += container.nounRepository.search(q).take(5).map {
                SearchResult("noun", it.id, it.kanji.ifBlank { it.hiragana }, it.hiragana, it.meaning)
            }
            results += container.grammarRepository.search(q).take(5).map {
                SearchResult("grammar", it.id, it.title, "Lesson ${it.lessonNumber}", it.content.take(60))
            }
            results += container.phraseRepository.search(q).take(5).map {
                SearchResult("phrase", it.id, it.phrase, it.reading, it.meaning)
            }
            results
        }
    }

    val isSearching = searchQuery.isNotBlank()

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Term Study",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showHelp = true }) {
                    Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (!isSearching) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search all content…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(20.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            if (isSearching) {
                // ── Search results ─────────────────────────────────────────────
                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No results for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        items(searchResults, key = { "${it.type}_${it.itemId}" }) { result ->
                            SearchResultRow(
                                result = result,
                                onClick = {
                                    when (result.type) {
                                        "kanji"     -> onKanjiClick?.invoke(result.itemId)
                                        "verb"      -> onVerbClick?.invoke(result.itemId)
                                        "adjective" -> onAdjectiveClick?.invoke(result.itemId)
                                        "noun"      -> onNounClick?.invoke(result.itemId)
                                        "grammar"   -> onGrammarClick?.invoke(result.itemId)
                                        "phrase"    -> onPhraseClick?.invoke(result.itemId)
                                    }
                                },
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                    }
                }
            } else {
                // ── Category cards ─────────────────────────────────────────────
                data class CategoryInfo(
                    val japanese: String,
                    val english: String,
                    val known: Int,
                    val total: Int,
                    val onClick: () -> Unit,
                )

                val allCategories = listOf(
                    CategoryInfo("文法",   "Grammar",      knownGrammar, totalGrammar, onGrammar),
                    CategoryInfo("語彙",   "Vocabulary",   knownVocab,   totalVocab,   onVocabulary),
                    CategoryInfo("動詞",   "Verbs",        knownVerb,    totalVerb,    onVerbs),
                    CategoryInfo("形容詞", "Adjectives",   knownAdj,     totalAdj,     onAdjectives),
                    CategoryInfo("名詞",   "Nouns",        knownNoun,    totalNoun,    onNouns),
                    CategoryInfo("漢字",   "Kanji",        knownKanji,   totalKanji,   onKanji),
                    CategoryInfo("部首",   "Radicals",     knownRadical, totalRadical, onRadicals),
                    CategoryInfo("擬音語", "Onomatopoeia", 0,            0,            onOnomatopoeia),
                )

                allCategories.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        row.forEach { info ->
                            CategoryCard(
                                modifier = Modifier.weight(1f),
                                japanese = info.japanese,
                                english = info.english,
                                known = info.known,
                                total = info.total,
                                onClick = info.onClick,
                            )
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(result: SearchResult, onClick: () -> Unit) {
    val canNavigate = onClick != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (canNavigate) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Type badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 6.dp, vertical = 3.dp),
        ) {
            Text(
                text = result.type.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (result.reading.isNotBlank() && result.reading != result.title) {
                Text(
                    text = result.reading,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = result.meaning,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CategoryCard(
    modifier: Modifier = Modifier,
    japanese: String,
    english: String,
    known: Int,
    total: Int,
    onClick: () -> Unit,
) {
    val progress = if (total > 0) known.toFloat() / total.toFloat() else 0f
    val pct = if (total > 0) (progress * 100).toInt() else 0

    Box(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = japanese,
                fontSize = 22.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = english,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (total > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                )
                Text(
                    text = "$known/$total known ($pct%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
