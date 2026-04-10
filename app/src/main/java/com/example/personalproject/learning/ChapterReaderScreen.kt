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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
fun ChapterReaderScreen(
    level: String,
    chapterIndex: Int,
    chapterType: String,
    setIndex: Int,
    chapterTitle: String,
    onBack: () -> Unit,
    onContinue: (chapterType: String, setIndex: Int, chapterTitle: String) -> Unit,
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
                    verbRepository = container.verbRepository,
                    adjectiveRepository = container.adjectiveRepository,
                    nounRepository = container.nounRepository,
                    phraseRepository = container.phraseRepository,
                    kanjiRepository = container.kanjiRepository,
                    progressRepository = container.chapterProgressRepository,
                )
            }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    if (state.isCompleted) {
        ChapterCompletionOverlay(
            chapterTitle = chapterTitle,
            hasNext = state.nextChapterType != null,
            onBack = onBack,
            onContinue = {
                onContinue(state.nextChapterType!!, state.nextSetIndex!!, state.nextChapterTitle!!)
            },
        )
        return
    }

    Scaffold(
        topBar = {
            KotobaTopBar(title = chapterTitle, onBack = onBack)
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            state.items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "No content available.", textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { vm.dispatchAction(ChapterReaderAction.CompleteChapter) }) {
                            Text("Mark Complete")
                        }
                    }
                }
            }
            else -> {
                val progress = (state.currentIndex + 1).toFloat() / state.items.size.toFloat()
                val currentItem = state.items.getOrNull(state.currentIndex)
                val isLast = state.currentIndex >= state.items.size - 1

                val isSaved by remember(currentItem) {
                    when (currentItem) {
                        is ChapterItem.GrammarItem -> container.savedRepository.isItemSavedFlow("grammar", currentItem.entry.id)
                        is ChapterItem.VocabItem -> container.savedRepository.isItemSavedFlow("vocab", currentItem.word.id)
                        is ChapterItem.StudyVocabItem -> container.savedRepository.isItemSavedFlow("vocab", currentItem.word.id)
                        is ChapterItem.TermStudyItem -> container.savedRepository.isItemSavedFlow(currentItem.type, currentItem.id.substringAfter("_"))
                        null -> flowOf(false)
                    }
                }.collectAsStateWithLifecycle(initialValue = false)

                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "${state.currentIndex + 1} / ${state.items.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.End).padding(end = 16.dp, top = 4.dp),
                    )

                    when (currentItem) {
                        is ChapterItem.GrammarItem -> GrammarItemContent(
                            entry = currentItem.entry,
                            isSaved = isSaved,
                            isLast = isLast,
                            onSave = {
                                scope.launch {
                                    container.savedRepository.toggle(
                                        type = "grammar",
                                        itemId = currentItem.entry.id,
                                        title = currentItem.entry.title,
                                        reading = "Lesson ${currentItem.entry.lessonNumber}",
                                        meaning = currentItem.entry.content.take(80),
                                    )
                                }
                            },
                            onNext = { vm.dispatchAction(ChapterReaderAction.NextItem) },
                            onComplete = { vm.dispatchAction(ChapterReaderAction.CompleteChapter) },
                        )
                        is ChapterItem.VocabItem -> VocabItemContent(
                            word = currentItem.word,
                            isSaved = isSaved,
                            isLast = isLast,
                            onSave = {
                                scope.launch {
                                    container.savedRepository.toggle(
                                        type = "vocab",
                                        itemId = currentItem.word.id,
                                        title = currentItem.word.japanese,
                                        reading = currentItem.word.hiragana,
                                        meaning = currentItem.word.english,
                                    )
                                }
                            },
                            onNext = { vm.dispatchAction(ChapterReaderAction.NextItem) },
                            onComplete = { vm.dispatchAction(ChapterReaderAction.CompleteChapter) },
                        )
                        is ChapterItem.StudyVocabItem -> StudyVocabItemContent(
                            word = currentItem.word,
                            isSaved = isSaved,
                            isRevealed = state.isRevealed,
                            isLast = isLast,
                            onReveal = { vm.dispatchAction(ChapterReaderAction.RevealStudyVocab) },
                            onSave = {
                                scope.launch {
                                    container.savedRepository.toggle(
                                        type = "vocab",
                                        itemId = currentItem.word.id,
                                        title = currentItem.word.japanese,
                                        reading = currentItem.word.hiragana,
                                        meaning = currentItem.word.english,
                                    )
                                }
                            },
                            onNext = { vm.dispatchAction(ChapterReaderAction.NextItem) },
                            onReviewAgain = { vm.dispatchAction(ChapterReaderAction.ReviewAgain) },
                            onComplete = { vm.dispatchAction(ChapterReaderAction.CompleteChapter) },
                        )
                        is ChapterItem.TermStudyItem -> TermStudyItemContent(
                            item = currentItem,
                            isSaved = isSaved,
                            isRevealed = state.isRevealed,
                            isLast = isLast,
                            onReveal = { vm.dispatchAction(ChapterReaderAction.RevealStudyVocab) },
                            onSave = {
                                scope.launch {
                                    container.savedRepository.toggle(
                                        type = currentItem.type,
                                        itemId = currentItem.id.substringAfter("_"),
                                        title = currentItem.displayScript,
                                        reading = currentItem.reading,
                                        meaning = currentItem.meaning,
                                    )
                                }
                            },
                            onNext = { vm.dispatchAction(ChapterReaderAction.NextItem) },
                            onReviewAgain = { vm.dispatchAction(ChapterReaderAction.ReviewAgain) },
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
private fun ChapterCompletionOverlay(
    chapterTitle: String,
    hasNext: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp),
            )
            Text(
                text = "Chapter Complete",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = chapterTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            if (hasNext) {
                Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                    Text("Next Chapter")
                    Spacer(Modifier.size(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back to Chapters")
            }
        }
    }
}

@Composable
private fun GrammarItemContent(
    entry: GrammarEntry,
    isSaved: Boolean,
    isLast: Boolean,
    onSave: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = entry.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onSave) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (isSaved) "Unsave" else "Save",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                modifier = Modifier.padding(16.dp),
            )
        }
        if (entry.exampleOne.isNotBlank()) {
            Text(text = "Example", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = entry.exampleOne,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
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
                    lineHeight = 22.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        if (entry.supportingContent.isNotBlank()) {
            Text(text = "Notes", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    text = entry.supportingContent,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = if (isLast) onComplete else onNext, modifier = Modifier.fillMaxWidth()) {
            Text(if (isLast) "Complete Chapter" else "Next")
        }
    }
}

@Composable
private fun VocabItemContent(
    word: VocabularyWord,
    isSaved: Boolean,
    isLast: Boolean,
    onSave: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(text = word.japanese, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                if (word.hiragana.isNotBlank() && word.hiragana != word.japanese) {
                    Text(text = word.hiragana, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
                if (word.romaji.isNotBlank()) {
                    Text(text = word.romaji, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = word.english, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (word.partOfSpeech.isNotBlank()) {
                        Text(text = word.partOfSpeech, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
            IconButton(onClick = onSave) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (isSaved) "Unsave" else "Save",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (word.exampleJapanese.isNotBlank()) {
            Text(text = "Example", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = word.exampleJapanese, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                    if (word.exampleEnglish.isNotBlank()) {
                        Text(text = word.exampleEnglish, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = if (isLast) onComplete else onNext, modifier = Modifier.fillMaxWidth()) {
            Text(if (isLast) "Complete Chapter" else "Next")
        }
    }
}

@Composable
private fun StudyVocabItemContent(
    word: VocabularyWord,
    isSaved: Boolean,
    isRevealed: Boolean,
    isLast: Boolean,
    onReveal: () -> Unit,
    onSave: () -> Unit,
    onNext: () -> Unit,
    onReviewAgain: () -> Unit,
    onComplete: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            onClick = { if (!isRevealed) onReveal() },
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = word.japanese, fontSize = 52.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
                if (isRevealed) {
                    Spacer(Modifier.height(16.dp))
                    if (word.hiragana.isNotBlank() && word.hiragana != word.japanese) {
                        Text(text = word.hiragana, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                    if (word.romaji.isNotBlank()) {
                        Text(text = word.romaji, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(text = word.english, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
                } else {
                    Spacer(Modifier.height(24.dp))
                    Text(text = "Tap to reveal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f))
                }
            }
        }

        if (isRevealed) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSave) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isSaved) "Unsave" else "Save",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                if (isLast) {
                    Button(onClick = onComplete, modifier = Modifier.weight(1f)) { Text("Complete Chapter") }
                } else {
                    OutlinedButton(onClick = onReviewAgain, modifier = Modifier.weight(1f)) { Text("Review Again") }
                    Button(onClick = onNext, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Got It") }
                }
            }
        }
    }
}

@Composable
private fun TermStudyItemContent(
    item: ChapterItem.TermStudyItem,
    isSaved: Boolean,
    isRevealed: Boolean,
    isLast: Boolean,
    onReveal: () -> Unit,
    onSave: () -> Unit,
    onNext: () -> Unit,
    onReviewAgain: () -> Unit,
    onComplete: () -> Unit,
) {
    val typeLabel = when (item.type) {
        "verb" -> "Verb"
        "adjective" -> "Adjective"
        "noun" -> "Noun"
        "phrase" -> "Phrase"
        "kanji" -> "Kanji"
        else -> "Vocabulary"
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = typeLabel.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            onClick = { if (!isRevealed) onReveal() },
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = item.displayScript, fontSize = 38.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
                if (isRevealed) {
                    Spacer(Modifier.height(16.dp))
                    if (item.reading.isNotBlank() && item.reading != item.displayScript) {
                        Text(text = item.reading, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                    if (item.romaji.isNotBlank() && item.romaji != item.reading) {
                        Text(text = item.romaji, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(text = item.meaning, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
                } else {
                    Spacer(Modifier.height(24.dp))
                    Text(text = "Tap to reveal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f))
                }
            }
        }

        if (isRevealed) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSave) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isSaved) "Unsave" else "Save",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                if (isLast) {
                    Button(onClick = onComplete, modifier = Modifier.weight(1f)) { Text("Complete Chapter") }
                } else {
                    OutlinedButton(onClick = onReviewAgain, modifier = Modifier.weight(1f)) { Text("Review Again") }
                    Button(onClick = onNext, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Got It") }
                }
            }
        }
    }
}
