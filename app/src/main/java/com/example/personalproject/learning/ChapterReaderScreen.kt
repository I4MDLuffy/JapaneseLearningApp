package com.example.personalproject.learning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.data.model.ChapterType
import com.example.personalproject.data.model.GrammarEntry
import com.example.personalproject.data.model.VocabularyWord
import com.example.personalproject.learning.mvi.ChapterItem
import com.example.personalproject.learning.mvi.ChapterReaderAction
import com.example.personalproject.learning.mvi.ChapterReaderViewModel
import com.example.personalproject.ui.components.KotobaTopBar

@Composable
fun ChapterReaderScreen(
    level: String,
    chapterIndex: Int,
    chapterType: String,
    setIndex: Int,
    chapterTitle: String,
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val type = ChapterType.valueOf(chapterType)
    val vm: ChapterReaderViewModel = viewModel(
        key = "${level}_${chapterIndex}",
        factory = viewModelFactory {
            initializer {
                ChapterReaderViewModel(
                    level = level,
                    chapterIndex = chapterIndex,
                    chapterType = type,
                    setIndex = setIndex,
                    chapterTitle = chapterTitle,
                    grammarRepository = container.grammarRepository,
                    vocabularyRepository = container.vocabularyRepository,
                    progressRepository = container.chapterProgressRepository,
                )
            }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) onBack()
    }

    Scaffold(
        topBar = {
            KotobaTopBar(title = chapterTitle, onBack = onBack)
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
            state.items.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No content available for this chapter.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { vm.dispatchAction(ChapterReaderAction.CompleteChapter) }) {
                            Text("Mark Complete")
                        }
                    }
                }
            }
            else -> {
                val progress = if (state.items.isNotEmpty())
                    (state.currentIndex + 1).toFloat() / state.items.size.toFloat()
                else 1f
                val currentItem = state.items.getOrNull(state.currentIndex)
                val isLast = state.currentIndex >= state.items.size - 1

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "${state.currentIndex + 1} / ${state.items.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = 16.dp, top = 4.dp),
                    )

                    when (currentItem) {
                        is ChapterItem.GrammarItem -> GrammarItemContent(
                            entry = currentItem.entry,
                            isLast = isLast,
                            onNext = { vm.dispatchAction(ChapterReaderAction.NextItem) },
                            onComplete = { vm.dispatchAction(ChapterReaderAction.CompleteChapter) },
                        )
                        is ChapterItem.VocabItem -> VocabItemContent(
                            word = currentItem.word,
                            isLast = isLast,
                            onNext = { vm.dispatchAction(ChapterReaderAction.NextItem) },
                            onComplete = { vm.dispatchAction(ChapterReaderAction.CompleteChapter) },
                        )
                        is ChapterItem.StudyVocabItem -> StudyVocabItemContent(
                            word = currentItem.word,
                            isRevealed = state.isRevealed,
                            isLast = isLast,
                            onReveal = { vm.dispatchAction(ChapterReaderAction.RevealStudyVocab) },
                            onNext = { vm.dispatchAction(ChapterReaderAction.NextItem) },
                            onComplete = { vm.dispatchAction(ChapterReaderAction.CompleteChapter) },
                        )
                        null -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun GrammarItemContent(
    entry: GrammarEntry,
    isLast: Boolean,
    onNext: () -> Unit,
    onComplete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = entry.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        }
        if (entry.exampleOne.isNotBlank()) {
            Text(
                text = "Example",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = entry.exampleOne,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        if (entry.exampleTwo.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = entry.exampleTwo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        if (entry.supportingContent.isNotBlank()) {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = entry.supportingContent,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
        }
        Spacer(Modifier.height(8.dp))
        if (isLast) {
            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Complete Chapter")
            }
        } else {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun VocabItemContent(
    word: VocabularyWord,
    isLast: Boolean,
    onNext: () -> Unit,
    onComplete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = word.japanese,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (word.hiragana.isNotBlank() && word.hiragana != word.japanese) {
                    Text(
                        text = word.hiragana,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
                if (word.romaji.isNotBlank()) {
                    Text(
                        text = word.romaji,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = word.english,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (word.partOfSpeech.isNotBlank()) {
                    Text(
                        text = word.partOfSpeech,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        }

        if (word.exampleJapanese.isNotBlank()) {
            Text(
                text = "Example",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = word.exampleJapanese,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                    )
                    if (word.exampleEnglish.isNotBlank()) {
                        Text(
                            text = word.exampleEnglish,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        if (isLast) {
            Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
                Text("Complete Chapter")
            }
        } else {
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text("Next")
            }
        }
    }
}

@Composable
private fun StudyVocabItemContent(
    word: VocabularyWord,
    isRevealed: Boolean,
    isLast: Boolean,
    onReveal: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            onClick = { if (!isRevealed) onReveal() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = word.japanese,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
                if (isRevealed) {
                    Spacer(Modifier.height(16.dp))
                    if (word.hiragana.isNotBlank() && word.hiragana != word.japanese) {
                        Text(
                            text = word.hiragana,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        )
                    }
                    if (word.romaji.isNotBlank()) {
                        Text(
                            text = word.romaji,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = word.english,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Tap to reveal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                    )
                }
            }
        }

        if (isRevealed) {
            if (isLast) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Complete Chapter")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onNext,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Review Again")
                    }
                    Button(
                        onClick = onNext,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Got It")
                    }
                }
            }
        }
    }
}
