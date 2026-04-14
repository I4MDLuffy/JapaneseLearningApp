package app.kotori.japanese.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.LocalContentColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.data.model.ChapterType
import app.kotori.japanese.data.model.GrammarEntry
import app.kotori.japanese.data.model.KanjiEntry
import app.kotori.japanese.data.model.VocabularyWord
import app.kotori.japanese.learning.mvi.ChapterItem
import app.kotori.japanese.learning.mvi.ChapterReaderAction
import app.kotori.japanese.learning.mvi.ChapterReaderViewModel
import app.kotori.japanese.learning.mvi.StudyCardMode
import app.kotori.japanese.ui.components.KotobaTopBar
import app.kotori.japanese.util.rememberTts
import app.kotori.japanese.util.swipeToNavigate
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
    val speak = rememberTts()

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

    val isStudyChapter = type == ChapterType.STUDY_VOCAB ||
            type == ChapterType.TERM_STUDY || type == ChapterType.KANJI

    Scaffold(
        topBar = {
            KotobaTopBar(
                title = chapterTitle,
                onBack = onBack,
                actions = if (isStudyChapter) {
                    {
                        TextButton(onClick = { vm.dispatchAction(ChapterReaderAction.ToggleStudyMode) }) {
                            Text(
                                text = if (state.studyCardMode == StudyCardMode.MULTIPLE_CHOICE) "Flashcard" else "MC",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                } else {
                    {}
                },
            )
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

                // Swipe-left = next action (or reveal for study cards); swipe-right = previous
                val onSwipeLeft: () -> Unit = {
                    val isStudyItem = currentItem is ChapterItem.StudyVocabItem ||
                            currentItem is ChapterItem.TermStudyItem ||
                            currentItem is ChapterItem.KanjiItem
                    val needsReveal = (currentItem is ChapterItem.StudyVocabItem || currentItem is ChapterItem.TermStudyItem) &&
                            state.studyCardMode == StudyCardMode.FLASHCARD && !state.isRevealed
                    val needsMcAnswer = isStudyItem && state.studyCardMode == StudyCardMode.MULTIPLE_CHOICE && state.selectedMcOption == null
                    when {
                        needsReveal -> vm.dispatchAction(ChapterReaderAction.RevealStudyVocab)
                        needsMcAnswer -> { /* Wait for user to select an option */ }
                        isLast -> vm.dispatchAction(ChapterReaderAction.CompleteChapter)
                        else -> vm.dispatchAction(ChapterReaderAction.NextItem)
                    }
                }

                val isSaved by remember(currentItem) {
                    when (currentItem) {
                        is ChapterItem.GrammarItem -> container.savedRepository.isItemSavedFlow("grammar", currentItem.entry.id)
                        is ChapterItem.VocabItem -> container.savedRepository.isItemSavedFlow("vocab", currentItem.word.id)
                        is ChapterItem.StudyVocabItem -> container.savedRepository.isItemSavedFlow("vocab", currentItem.word.id)
                        is ChapterItem.KanjiItem -> container.savedRepository.isItemSavedFlow("kanji", currentItem.entry.id)
                        is ChapterItem.TermStudyItem -> container.savedRepository.isItemSavedFlow(currentItem.type, currentItem.id.substringAfter("_"))
                        null -> flowOf(false)
                    }
                }.collectAsStateWithLifecycle(initialValue = false)

                val isKnown by remember(currentItem) {
                    when (currentItem) {
                        is ChapterItem.GrammarItem -> container.knownRepository.isItemKnownFlow("grammar", currentItem.entry.id)
                        is ChapterItem.TermStudyItem -> container.knownRepository.isItemKnownFlow(currentItem.type, currentItem.id.substringAfter("_"))
                        is ChapterItem.KanjiItem -> container.knownRepository.isItemKnownFlow("kanji", currentItem.entry.id)
                        else -> flowOf(false)
                    }
                }.collectAsStateWithLifecycle(initialValue = false)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .swipeToNavigate(
                            onSwipeLeft = onSwipeLeft,
                            onSwipeRight = { vm.dispatchAction(ChapterReaderAction.PreviousItem) },
                        ),
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
                        modifier = Modifier.align(Alignment.End).padding(end = 16.dp, top = 4.dp),
                    )

                    when (currentItem) {
                        is ChapterItem.KanjiItem -> KanjiItemContent(
                            entry = currentItem.entry,
                            isSaved = isSaved,
                            isKnown = isKnown,
                            isLast = isLast,
                            speak = speak,
                            studyCardMode = state.studyCardMode,
                            mcOptions = state.mcOptions,
                            selectedMcOption = state.selectedMcOption,
                            mcIsCorrect = state.mcIsCorrect,
                            onSelectMcOption = { vm.dispatchAction(ChapterReaderAction.SelectMcOption(it)) },
                            onSave = {
                                scope.launch {
                                    container.savedRepository.toggle(
                                        type = "kanji",
                                        itemId = currentItem.entry.id,
                                        title = currentItem.entry.kanji,
                                        reading = currentItem.entry.hiragana,
                                        meaning = currentItem.entry.meaning,
                                    )
                                }
                            },
                            onKnown = {
                                scope.launch { container.knownRepository.toggle("kanji", currentItem.entry.id) }
                            },
                            onNext = { vm.dispatchAction(ChapterReaderAction.NextItem) },
                            onComplete = { vm.dispatchAction(ChapterReaderAction.CompleteChapter) },
                        )
                        is ChapterItem.GrammarItem -> GrammarItemContent(
                            entry = currentItem.entry,
                            isSaved = isSaved,
                            isKnown = isKnown,
                            isLast = isLast,
                            speak = speak,
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
                            onKnown = {
                                scope.launch { container.knownRepository.toggle("grammar", currentItem.entry.id) }
                            },
                            onNext = { vm.dispatchAction(ChapterReaderAction.NextItem) },
                            onComplete = { vm.dispatchAction(ChapterReaderAction.CompleteChapter) },
                        )
                        is ChapterItem.VocabItem -> VocabItemContent(
                            word = currentItem.word,
                            isSaved = isSaved,
                            isLast = isLast,
                            speak = speak,
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
                            speak = speak,
                            studyCardMode = state.studyCardMode,
                            mcOptions = state.mcOptions,
                            selectedMcOption = state.selectedMcOption,
                            mcIsCorrect = state.mcIsCorrect,
                            onReveal = { vm.dispatchAction(ChapterReaderAction.RevealStudyVocab) },
                            onSelectMcOption = { vm.dispatchAction(ChapterReaderAction.SelectMcOption(it)) },
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
                            isKnown = isKnown,
                            isRevealed = state.isRevealed,
                            isLast = isLast,
                            speak = speak,
                            studyCardMode = state.studyCardMode,
                            mcOptions = state.mcOptions,
                            selectedMcOption = state.selectedMcOption,
                            mcIsCorrect = state.mcIsCorrect,
                            onReveal = { vm.dispatchAction(ChapterReaderAction.RevealStudyVocab) },
                            onSelectMcOption = { vm.dispatchAction(ChapterReaderAction.SelectMcOption(it)) },
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
                            onKnown = {
                                scope.launch { container.knownRepository.toggle(currentItem.type, currentItem.id.substringAfter("_")) }
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
    isKnown: Boolean,
    isLast: Boolean,
    speak: (String) -> Unit,
    onSave: () -> Unit,
    onKnown: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = entry.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { speak(entry.title) }) {
                Icon(Icons.Outlined.VolumeUp, contentDescription = "Pronounce", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            }
            IconButton(onClick = onKnown) {
                Icon(
                    imageVector = if (isKnown) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isKnown) "Mark as unknown" else "Mark as known",
                    tint = if (isKnown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
                Row(modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.Top) {
                    Text(
                        text = entry.exampleOne,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { speak(entry.exampleOne) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.VolumeUp, contentDescription = "Pronounce", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    }
                }
            }
        }
        if (entry.exampleTwo.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.Top) {
                    Text(
                        text = entry.exampleTwo,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { speak(entry.exampleTwo) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.VolumeUp, contentDescription = "Pronounce", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    }
                }
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
    speak: (String) -> Unit,
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = word.japanese, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    IconButton(onClick = { speak(word.japanese) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.VolumeUp, contentDescription = "Pronounce", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                    }
                }
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
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = word.exampleJapanese, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, modifier = Modifier.weight(1f))
                        IconButton(onClick = { speak(word.exampleJapanese) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.VolumeUp, contentDescription = "Pronounce", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        }
                    }
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
    speak: (String) -> Unit,
    studyCardMode: StudyCardMode,
    mcOptions: List<String>,
    selectedMcOption: String?,
    mcIsCorrect: Boolean?,
    onReveal: () -> Unit,
    onSelectMcOption: (String) -> Unit,
    onSave: () -> Unit,
    onNext: () -> Unit,
    onReviewAgain: () -> Unit,
    onComplete: () -> Unit,
) {
    if (studyCardMode == StudyCardMode.MULTIPLE_CHOICE && mcOptions.isNotEmpty()) {
        MultipleChoiceCardContent(
            question = word.japanese,
            reading = if (word.hiragana != word.japanese) word.hiragana else "",
            correctAnswer = word.english,
            options = mcOptions,
            selectedOption = selectedMcOption,
            isCorrect = mcIsCorrect,
            isSaved = isSaved,
            isLast = isLast,
            speak = speak,
            onSelect = onSelectMcOption,
            onSave = onSave,
            onNext = onNext,
            onComplete = onComplete,
        )
    } else {
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = word.japanese, fontSize = 52.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
                        IconButton(onClick = { speak(word.japanese) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.VolumeUp, contentDescription = "Pronounce", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                        }
                    }
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
}

@Composable
private fun TermStudyItemContent(
    item: ChapterItem.TermStudyItem,
    isSaved: Boolean,
    isKnown: Boolean,
    isRevealed: Boolean,
    isLast: Boolean,
    speak: (String) -> Unit,
    studyCardMode: StudyCardMode,
    mcOptions: List<String>,
    selectedMcOption: String?,
    mcIsCorrect: Boolean?,
    onReveal: () -> Unit,
    onSelectMcOption: (String) -> Unit,
    onSave: () -> Unit,
    onKnown: () -> Unit,
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

    if (studyCardMode == StudyCardMode.MULTIPLE_CHOICE && mcOptions.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = typeLabel.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
            MultipleChoiceCardContent(
                question = item.displayScript,
                reading = if (item.reading != item.displayScript) item.reading else "",
                correctAnswer = item.meaning,
                options = mcOptions,
                selectedOption = selectedMcOption,
                isCorrect = mcIsCorrect,
                isSaved = isSaved,
                isKnown = isKnown,
                isLast = isLast,
                speak = speak,
                onSelect = onSelectMcOption,
                onSave = onSave,
                onKnown = onKnown,
                onNext = onNext,
                onComplete = onComplete,
            )
        }
    } else {
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = item.displayScript, fontSize = 38.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
                        IconButton(onClick = { speak(item.displayScript) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.VolumeUp, contentDescription = "Pronounce", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                        }
                    }
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
                    IconButton(onClick = onKnown) {
                        Icon(
                            imageVector = if (isKnown) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (isKnown) "Mark as unknown" else "Mark as known",
                            tint = if (isKnown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KanjiItemContent(
    entry: app.kotori.japanese.data.model.KanjiEntry,
    isSaved: Boolean,
    isKnown: Boolean,
    isLast: Boolean,
    speak: (String) -> Unit,
    studyCardMode: StudyCardMode,
    mcOptions: List<String>,
    selectedMcOption: String?,
    mcIsCorrect: Boolean?,
    onSelectMcOption: (String) -> Unit,
    onSave: () -> Unit,
    onKnown: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit,
) {
    if (studyCardMode == StudyCardMode.MULTIPLE_CHOICE && mcOptions.isNotEmpty()) {
        MultipleChoiceCardContent(
            question = entry.kanji,
            reading = entry.hiragana,
            correctAnswer = entry.meaning,
            options = mcOptions,
            selectedOption = selectedMcOption,
            isCorrect = mcIsCorrect,
            isSaved = isSaved,
            isKnown = isKnown,
            isLast = isLast,
            speak = speak,
            onSelect = onSelectMcOption,
            onSave = onSave,
            onKnown = onKnown,
            onNext = onNext,
            onComplete = onComplete,
            modifier = Modifier.padding(16.dp),
        )
        return
    }

    val kanji = entry
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Hero card — large kanji character + meaning
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = kanji.kanji,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                IconButton(
                    onClick = { speak(kanji.kanji) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VolumeUp,
                        contentDescription = "Pronounce",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    )
                }
                Text(
                    text = kanji.meaning,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Readings row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (kanji.onYomi.isNotEmpty()) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "ON'YOMI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                        Text(text = kanji.onYomi.joinToString("、"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                }
            }
            if (kanji.kunYomi.isNotEmpty()) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "KUN'YOMI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                        Text(text = kanji.kunYomi.joinToString("、"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Tags: JLPT level + grade level
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (kanji.jlptLevel.isNotBlank()) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(kanji.jlptLevel) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            }
            if (kanji.gradeLevel.isNotBlank()) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("Grade ${kanji.gradeLevel}") },
                )
            }
            if (kanji.strokeCount > 0) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("${kanji.strokeCount} strokes") },
                )
            }
        }

        // Hiragana reading
        if (kanji.hiragana.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "READING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                    Text(text = kanji.hiragana, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // Bottom action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onKnown) {
                Icon(
                    imageVector = if (isKnown) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (isKnown) "Mark as unknown" else "Mark as known",
                    tint = if (isKnown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) { Text("Next") }
            }
        }
    }
}

// ── Shared Multiple Choice UI ─────────────────────────────────────────────────

@Composable
private fun MultipleChoiceCardContent(
    question: String,
    reading: String,
    correctAnswer: String,
    options: List<String>,
    selectedOption: String?,
    isCorrect: Boolean?,
    isSaved: Boolean,
    isLast: Boolean,
    speak: (String) -> Unit,
    onSelect: (String) -> Unit,
    onSave: () -> Unit,
    onNext: () -> Unit,
    isKnown: Boolean = false,
    onKnown: (() -> Unit)? = null,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Question card
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = question,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
                IconButton(
                    onClick = { speak(question) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VolumeUp,
                        contentDescription = "Pronounce",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    )
                }
                if (reading.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = reading,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                }
                if (selectedOption != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = correctAnswer,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // Options grid — 2 columns
        val optionRows = options.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            optionRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { option ->
                        val bgColor by animateColorAsState(
                            targetValue = when {
                                selectedOption == null -> MaterialTheme.colorScheme.surfaceVariant
                                option == correctAnswer -> Color(0xFF4CAF50)
                                option == selectedOption -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            label = "mc_option_$option",
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .clickable(enabled = selectedOption == null) { onSelect(option) }
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = when {
                                    selectedOption != null && (option == correctAnswer || option == selectedOption) -> Color.White
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                    // Pad odd row
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        // Action row — shown after answering
        if (selectedOption != null) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (onKnown != null) {
                    IconButton(onClick = onKnown) {
                        Icon(
                            imageVector = if (isKnown) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (isKnown) "Mark as unknown" else "Mark as known",
                            tint = if (isKnown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
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
                    Button(
                        onClick = onNext,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCorrect == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        ),
                    ) {
                        Text(if (isCorrect == true) "Correct! Next" else "Next")
                    }
                }
            }
        }
    }
}
