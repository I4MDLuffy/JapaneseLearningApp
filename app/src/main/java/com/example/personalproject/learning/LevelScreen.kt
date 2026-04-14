package com.example.personalproject.learning

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.data.model.Chapter
import com.example.personalproject.data.model.ChapterType
import com.example.personalproject.learning.mvi.LevelAction
import com.example.personalproject.learning.mvi.LevelViewModel
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.components.ScreenHelpDialog

@Composable
fun LevelScreen(
    level: String,
    onBack: () -> Unit,
    onChapter: (level: String, chapterIndex: Int, chapterType: String, setIndex: Int, chapterTitle: String) -> Unit,
) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }

    val vm: LevelViewModel = viewModel(
        key = level,
        factory = viewModelFactory {
            initializer {
                LevelViewModel(
                    level = level,
                    grammarRepository = container.grammarRepository,
                    vocabularyRepository = container.vocabularyRepository,
                    kanjiRepository = container.kanjiRepository,
                    progressRepository = container.chapterProgressRepository,
                )
            }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    val levelHelpDescription = when (level) {
        "beginner" -> "The Beginner section covers JLPT N5 material — the foundations of Japanese.\n\n" +
            "Each lesson follows a five-step cycle:\n" +
            "1. Grammar — read the grammar explanation\n" +
            "2. Kanji — study individual N5 kanji characters\n" +
            "3. Vocabulary — learn new words (single-kanji and kana words to build familiarity gradually)\n" +
            "4. Term Study — review terms from the lesson\n" +
            "5. Study Vocab — flashcard review of the vocabulary set\n\n" +
            "Completed chapters are marked with a check. Locked chapters (🔒) unlock after the previous chapter is complete."
        "intermediate" -> "The Intermediate section covers JLPT N4–N3 material, building on the basics.\n\n" +
            "Each lesson follows a five-step cycle: Grammar → Kanji → Vocabulary → Term Study → Study Vocab.\n\n" +
            "Chapters introduce more complex grammar patterns, expanded vocabulary, and kanji. " +
            "Completed chapters are marked with a check."
        "advanced" -> "The Advanced section covers JLPT N2–N1 material for learners approaching fluency.\n\n" +
            "Each lesson follows a five-step cycle: Grammar → Kanji → Vocabulary → Term Study → Study Vocab.\n\n" +
            "Chapters cover nuanced grammar, formal expressions, and a broad range of kanji. Tap a chapter to open it."
        else -> "The Master section contains native-level material for learners aiming for full fluency.\n\n" +
            "Each lesson follows a five-step cycle: Grammar → Kanji → Vocabulary → Term Study → Study Vocab.\n\n" +
            "Chapters cover advanced expressions, classical forms, and domain-specific vocabulary. Tap a chapter to open it."
    }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("level_$level")) {
            container.onboardingRepository.markScreenSeen("level_$level")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = state.levelName.ifBlank { level.replaceFirstChar { it.uppercase() } },
            description = levelHelpDescription,
            onDismiss = { showHelp = false },
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            KotobaTopBar(
                title = state.levelName,
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showHelp = true }) {
                        Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                    }
                },
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            state.chapters.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No chapters available for this level yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    items(state.chapters) { chapter ->
                        ChapterRow(
                            chapter = chapter,
                            onClick = {
                                if (chapter.isUnlocked) {
                                    onChapter(
                                        level,
                                        chapter.index,
                                        chapter.type.name,
                                        chapter.setIndex,
                                        chapter.title,
                                    )
                                }
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterRow(chapter: Chapter, onClick: () -> Unit) {
    val contentAlpha = if (chapter.isUnlocked) 1f else 0.4f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = chapter.isUnlocked, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            imageVector = chapterIcon(chapter.type),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha),
            modifier = Modifier.size(24.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chapter.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = contentAlpha),
            )
            Text(
                text = chapterTypeLabel(chapter.type),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = contentAlpha * 0.7f),
            )
        }
        Icon(
            imageVector = when {
                chapter.isCompleted -> Icons.Default.Check
                !chapter.isUnlocked -> Icons.Default.Lock
                else -> Icons.Default.Book
            },
            contentDescription = null,
            tint = when {
                chapter.isCompleted -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onBackground.copy(alpha = contentAlpha * 0.5f)
            },
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun chapterIcon(type: ChapterType): ImageVector = when (type) {
    ChapterType.GRAMMAR -> Icons.AutoMirrored.Filled.MenuBook
    ChapterType.KANJI -> Icons.Default.Book
    ChapterType.VOCAB -> Icons.Default.Book
    ChapterType.STUDY_VOCAB -> Icons.Default.School
    ChapterType.TERM_STUDY -> Icons.Default.School
}

private fun chapterTypeLabel(type: ChapterType): String = when (type) {
    ChapterType.GRAMMAR -> "Read grammar explanations"
    ChapterType.KANJI -> "Study kanji characters"
    ChapterType.VOCAB -> "Learn new vocabulary"
    ChapterType.STUDY_VOCAB -> "Study vocabulary with flashcards"
    ChapterType.TERM_STUDY -> "Study terms from this lesson"
}
