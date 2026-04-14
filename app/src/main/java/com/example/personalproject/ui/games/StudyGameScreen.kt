package com.example.personalproject.ui.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.ImeAction
import com.example.personalproject.ui.games.mvi.FillBlankDirection
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.games.mvi.GameType
import com.example.personalproject.ui.games.mvi.KanjiBuilderAction
import com.example.personalproject.ui.games.mvi.KanjiBuilderPhase
import com.example.personalproject.ui.games.mvi.KanjiBuilderViewModel
import com.example.personalproject.ui.games.mvi.KanjiDropAction
import com.example.personalproject.ui.games.mvi.KanjiDropMode
import com.example.personalproject.ui.games.mvi.KanjiDropPhase
import com.example.personalproject.ui.games.mvi.KanjiDropViewModel
import com.example.personalproject.ui.games.mvi.PairMode
import com.example.personalproject.ui.games.mvi.StudyFeedback
import com.example.personalproject.ui.games.mvi.StudyGameAction
import com.example.personalproject.ui.games.mvi.StudyGamePhase
import com.example.personalproject.ui.games.mvi.StudyGameViewModel

@Composable
fun StudyGameScreen(
    gameType: String,
    setKey: String,
    onBack: () -> Unit,
) {
    val type = GameType.valueOf(gameType)
    when (type) {
        GameType.KANJI_DROP -> KanjiDropGameScreen(jlptFilter = setKey, onBack = onBack)
        GameType.KANJI_BUILDER -> KanjiBuilderGameScreen(jlptFilter = setKey, onBack = onBack)
        else -> StudyGameScreenInner(type = type, setKey = setKey, onBack = onBack)
    }
}

// ── Vocabulary-based games (Flashcards, Timed Quiz, Match Pairs, Kana Speed, Kana Swipe) ──────

@Composable
private fun StudyGameScreenInner(
    type: GameType,
    setKey: String,
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val vm: StudyGameViewModel = viewModel(
        key = "${type}_${setKey}",
        factory = viewModelFactory {
            initializer {
                StudyGameViewModel(
                    gameType = type,
                    setKey = setKey,
                    container = container,
                )
            }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    val title = when (type) {
        GameType.FLASHCARDS -> "Flashcards"
        GameType.TIMED_QUIZ -> "Timed Quiz"
        GameType.MATCH_PAIRS -> "Match Pairs"
        GameType.KANA_SPEED -> "Speed Round"
        GameType.KANA_SWIPE -> "Kana Swipe"
        else -> ""
    }

    Scaffold(
        topBar = { KotobaTopBar(title = title, onBack = onBack) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val phase = state.phase) {
                is StudyGamePhase.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is StudyGamePhase.Flashcard -> FlashcardContent(
                    phase = phase,
                    onFlip = { vm.dispatchAction(StudyGameAction.FlipCard) },
                    onGotIt = { vm.dispatchAction(StudyGameAction.GotIt) },
                    onReview = { vm.dispatchAction(StudyGameAction.ReviewAgain) },
                )

                is StudyGamePhase.TimedQuiz -> TimedQuizContent(
                    phase = phase,
                    onSelect = { vm.dispatchAction(StudyGameAction.SelectOption(it)) },
                )

                is StudyGamePhase.MatchPairs -> MatchPairsContent(
                    phase = phase,
                    pairMode = state.pairMode,
                    onCardTap = { vm.dispatchAction(StudyGameAction.TapCard(it)) },
                    onModeChange = { vm.dispatchAction(StudyGameAction.SetPairMode(it)) },
                )

                is StudyGamePhase.KanaSpeed -> KanaSpeedContent(
                    phase = phase,
                    onTapTile = { vm.dispatchAction(StudyGameAction.TapGridTile(it)) },
                )

                is StudyGamePhase.KanaSwipe -> KanaSwipeContent(
                    phase = phase,
                    onTapTile = { vm.dispatchAction(StudyGameAction.TapSwipeTile(it)) },
                    onUndo = { vm.dispatchAction(StudyGameAction.UndoSwipe) },
                    onSubmit = { vm.dispatchAction(StudyGameAction.SubmitSwipe) },
                )

                is StudyGamePhase.FillBlank -> FillBlankContent(
                    phase = phase,
                    onDirectionChange = { vm.dispatchAction(StudyGameAction.SetFillBlankDirection(it)) },
                    onInputChange = { vm.dispatchAction(StudyGameAction.UpdateFillBlankInput(it)) },
                    onSubmit = { vm.dispatchAction(StudyGameAction.SubmitFillBlank) },
                    onNext = { vm.dispatchAction(StudyGameAction.NextFillBlank) },
                )

                is StudyGamePhase.Results -> ResultsContent(
                    phase = phase,
                    onRestart = { vm.dispatchAction(StudyGameAction.Restart) },
                    onBack = onBack,
                )
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
            }
        }
    }
}

// ── Flashcard ─────────────────────────────────────────────────────────────────

@Composable
private fun FlashcardContent(
    phase: StudyGamePhase.Flashcard,
    onFlip: () -> Unit,
    onGotIt: () -> Unit,
    onReview: () -> Unit,
) {
    val item = phase.entries.getOrNull(phase.currentIndex) ?: return
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProgressBar(phase.currentIndex, phase.entries.size)

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            onClick = { if (!phase.isRevealed) onFlip() },
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = item.question,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
                if (phase.isRevealed) {
                    Spacer(Modifier.height(12.dp))
                    if (item.reading.isNotBlank() && item.reading != item.question) {
                        Text(
                            text = item.reading,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        )
                    }
                    Text(
                        text = item.romaji,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = item.answer,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Tap to reveal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                    )
                }
            }
        }

        if (phase.isRevealed) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onReview, modifier = Modifier.weight(1f)) { Text("Review Again") }
                Button(
                    onClick = onGotIt,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) { Text("Got It") }
            }
        }
    }
}

// ── Timed Quiz ────────────────────────────────────────────────────────────────

@Composable
private fun TimedQuizContent(
    phase: StudyGamePhase.TimedQuiz,
    onSelect: (String) -> Unit,
) {
    val item = phase.entries.getOrNull(phase.currentIndex) ?: return
    val timerColor by animateColorAsState(
        targetValue = when {
            phase.timeRemaining > 0.5f -> MaterialTheme.colorScheme.primary
            phase.timeRemaining > 0.25f -> Color(0xFFFF9800)
            else -> MaterialTheme.colorScheme.error
        },
        label = "timer",
    )
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProgressBar(phase.currentIndex, phase.entries.size)
        LinearProgressIndicator(
            progress = { phase.timeRemaining },
            modifier = Modifier.fillMaxWidth(),
            color = timerColor,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = item.question,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (item.reading.isNotBlank() && item.reading != item.question) {
                    Text(
                        text = item.reading,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            phase.options.forEach { option ->
                val bgColor by animateColorAsState(
                    targetValue = when {
                        phase.selectedOption == null -> MaterialTheme.colorScheme.surfaceVariant
                        option == item.answer -> Color(0xFF4CAF50)
                        option == phase.selectedOption -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    label = "option_$option",
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable(enabled = phase.selectedOption == null) { onSelect(option) }
                        .padding(16.dp),
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

// ── Match Pairs ───────────────────────────────────────────────────────────────

@Composable
private fun MatchPairsContent(
    phase: StudyGamePhase.MatchPairs,
    pairMode: PairMode,
    onCardTap: (Int) -> Unit,
    onModeChange: (PairMode) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = pairMode == PairMode.ENGLISH,
                onClick = { onModeChange(PairMode.ENGLISH) },
                label = { Text("English") },
                modifier = Modifier.weight(1f),
            )
            FilterChip(
                selected = pairMode == PairMode.ROMAJI,
                onClick = { onModeChange(PairMode.ROMAJI) },
                label = { Text("Romaji") },
                modifier = Modifier.weight(1f),
            )
        }

        Text(
            text = "Batch ${phase.batchIndex + 1} / ${phase.totalBatches}  •  Matched: ${phase.matchedIds.size} / ${phase.cards.size / 2}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            itemsIndexed(phase.cards) { index, card ->
                val isMatched = phase.matchedIds.contains(card.pairId)
                val isSelected = phase.firstSelectedIndex == index
                val isWrong = phase.wrongIndices.contains(index)
                val bg by animateColorAsState(
                    targetValue = when {
                        isMatched -> Color(0xFF4CAF50)
                        isWrong -> MaterialTheme.colorScheme.error
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    label = "card_$index",
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bg)
                        .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)) else Modifier)
                        .clickable(enabled = !isMatched && phase.wrongIndices.isEmpty()) { onCardTap(index) }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = card.text,
                        style = if (card.isJapanese) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodySmall,
                        fontWeight = if (card.isJapanese) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = when {
                            isMatched || isWrong -> Color.White
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

// ── Kana Speed ────────────────────────────────────────────────────────────────

@Composable
private fun KanaSpeedContent(
    phase: StudyGamePhase.KanaSpeed,
    onTapTile: (Int) -> Unit,
) {
    val item = phase.entries.getOrNull(phase.currentIndex) ?: return
    val target = HiraganaUtils.decompose(item.reading)
    val answerSoFar = phase.tappedIndices.joinToString("") { phase.gridTiles.getOrElse(it) { "" } }

    val timerColor by animateColorAsState(
        targetValue = when {
            phase.timeRemaining > 0.5f -> MaterialTheme.colorScheme.primary
            phase.timeRemaining > 0.25f -> Color(0xFFFF9800)
            else -> MaterialTheme.colorScheme.error
        },
        label = "speed_timer",
    )
    val cardBgTarget = when (phase.feedback) {
        StudyFeedback.CORRECT -> Color(0xFF4CAF50)
        StudyFeedback.WRONG -> MaterialTheme.colorScheme.error
        null -> MaterialTheme.colorScheme.primaryContainer
    }
    val cardBg by animateColorAsState(targetValue = cardBgTarget, label = "card_bg")

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ProgressBar(phase.currentIndex, phase.entries.size)
        LinearProgressIndicator(
            progress = { phase.timeRemaining },
            modifier = Modifier.fillMaxWidth(),
            color = timerColor,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBg),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = item.question,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(8.dp))
                // Answer-so-far display
                Row(horizontalArrangement = Arrangement.Center) {
                    target.forEachIndexed { i, ch ->
                        val tapped = i < phase.tappedIndices.size
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (tapped) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                                )
                                .padding(2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (tapped) phase.gridTiles.getOrElse(phase.tappedIndices[i]) { "_" } else "_",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        if (i < target.size - 1) Spacer(Modifier.width(4.dp))
                    }
                }
                if (phase.feedback != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (phase.feedback == StudyFeedback.CORRECT) "Correct!" else "Answer: ${item.reading}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
        }

        // 3×4 kana tile grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            itemsIndexed(phase.gridTiles) { index, tile ->
                val isTapped = phase.tappedIndices.contains(index)
                val bg by animateColorAsState(
                    targetValue = when {
                        isTapped -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        phase.feedback != null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    label = "tile_$index",
                )
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bg)
                        .clickable(enabled = !isTapped && phase.feedback == null) { onTapTile(index) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tile,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ── Kana Swipe ────────────────────────────────────────────────────────────────

@Composable
private fun KanaSwipeContent(
    phase: StudyGamePhase.KanaSwipe,
    onTapTile: (Int) -> Unit,
    onUndo: () -> Unit,
    onSubmit: () -> Unit,
) {
    val item = phase.entries.getOrNull(phase.currentIndex) ?: return
    val target = HiraganaUtils.decompose(item.reading)

    val cardBgTarget = when (phase.feedback) {
        StudyFeedback.CORRECT -> Color(0xFF4CAF50)
        StudyFeedback.WRONG -> MaterialTheme.colorScheme.error
        null -> MaterialTheme.colorScheme.primaryContainer
    }
    val cardBg by animateColorAsState(targetValue = cardBgTarget, label = "swipe_bg")

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProgressBar(phase.currentIndex, phase.entries.size)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardBg),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = item.answer,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                // Current path display
                val pathText = if (phase.path.isEmpty()) "—" else phase.path.joinToString("") { phase.tiles.getOrElse(it) { "" } }
                Text(
                    text = pathText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = if (phase.path.isEmpty()) 0.3f else 1f),
                    letterSpacing = 4.sp,
                )
                if (phase.feedback == StudyFeedback.WRONG) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Answer: ${item.reading}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }
        }

        // 4×2 hiragana tile grid (8 tiles)
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(phase.tiles) { index, tile ->
                val isInPath = phase.path.contains(index)
                val pathPosition = phase.path.indexOf(index) + 1
                val bg by animateColorAsState(
                    targetValue = when {
                        isInPath -> MaterialTheme.colorScheme.primary
                        phase.feedback != null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    label = "swipe_tile_$index",
                )
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .clickable(enabled = !isInPath && phase.feedback == null) { onTapTile(index) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tile,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isInPath) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (isInPath) {
                        Text(
                            text = pathPosition.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onUndo,
                modifier = Modifier.weight(1f),
                enabled = phase.path.isNotEmpty() && phase.feedback == null,
            ) {
                Text("Undo")
            }
            Button(
                onClick = onSubmit,
                modifier = Modifier.weight(2f),
                enabled = phase.path.size == target.size && phase.feedback == null,
            ) {
                Text("Submit (${phase.path.size}/${target.size})")
            }
        }
    }
}

// ── Fill in the Blank ─────────────────────────────────────────────────────────

@Composable
private fun FillBlankContent(
    phase: StudyGamePhase.FillBlank,
    onDirectionChange: (FillBlankDirection) -> Unit,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit,
) {
    val item = phase.entries.getOrNull(phase.currentIndex) ?: return
    val progress = (phase.currentIndex + 1).toFloat() / phase.entries.size.toFloat()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Progress
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        Text(
            text = "${phase.currentIndex + 1} / ${phase.entries.size}   Score: ${phase.score}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )

        // Direction selector
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = phase.direction == FillBlankDirection.JP_TO_EN,
                onClick = { onDirectionChange(FillBlankDirection.JP_TO_EN) },
                label = { Text("Japanese → English") },
            )
            FilterChip(
                selected = phase.direction == FillBlankDirection.EN_TO_JP,
                onClick = { onDirectionChange(FillBlankDirection.EN_TO_JP) },
                label = { Text("English → Romaji") },
            )
        }

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
                val questionText = if (phase.direction == FillBlankDirection.JP_TO_EN) item.question else item.answer
                val hintText = if (phase.direction == FillBlankDirection.JP_TO_EN && item.reading.isNotBlank() && item.reading != item.question) item.reading else ""
                Text(
                    text = questionText,
                    fontSize = if (phase.direction == FillBlankDirection.JP_TO_EN) 48.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
                if (hintText.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = hintText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                }
                if (phase.isSubmitted) {
                    Spacer(Modifier.height(16.dp))
                    val correctAnswer = if (phase.direction == FillBlankDirection.JP_TO_EN) item.answer else "${item.reading} / ${item.romaji}"
                    Text(
                        text = "✓ $correctAnswer",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (phase.isCorrect == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // Input + submit
        val hintLabel = if (phase.direction == FillBlankDirection.JP_TO_EN) "Type the English meaning" else "Type the romaji reading"
        OutlinedTextField(
            value = phase.inputText,
            onValueChange = { if (!phase.isSubmitted) onInputChange(it) },
            label = { Text(hintLabel) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !phase.isSubmitted,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (!phase.isSubmitted) onSubmit() }),
            isError = phase.isSubmitted && phase.isCorrect == false,
        )

        if (!phase.isSubmitted) {
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = phase.inputText.isNotBlank(),
            ) {
                Text("Submit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (phase.isCorrect == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = if (phase.isCorrect == true) "Correct! Next →" else "Next →",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ── Results ───────────────────────────────────────────────────────────────────

@Composable
private fun ResultsContent(
    phase: StudyGamePhase.Results,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "${phase.score} / ${phase.total}",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Score",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text("Play Again") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Games") }
    }
}

// ── Kanji Drop game screen ─────────────────────────────────────────────────────

@Composable
fun KanjiDropGameScreen(jlptFilter: String = "all", onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: KanjiDropViewModel = viewModel(
        key = "drop_$jlptFilter",
        factory = viewModelFactory {
            initializer { KanjiDropViewModel(container.kanjiRepository, jlptFilter) }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { KotobaTopBar(title = "Kanji Drop", onBack = onBack) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val phase = state.phase) {
                is KanjiDropPhase.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is KanjiDropPhase.Playing -> KanjiDropPlayingContent(
                    phase = phase,
                    mode = state.mode,
                    onSelectAnswer = { vm.dispatchAction(KanjiDropAction.SelectAnswer(it)) },
                    onSetMode = { vm.dispatchAction(KanjiDropAction.SetMode(it)) },
                )

                is KanjiDropPhase.Results -> KanjiDropResultsContent(
                    phase = phase,
                    onRestart = { vm.dispatchAction(KanjiDropAction.Restart) },
                    onBack = onBack,
                )
            }
        }
    }
}

@Composable
private fun KanjiDropPlayingContent(
    phase: KanjiDropPhase.Playing,
    mode: KanjiDropMode,
    onSelectAnswer: (String) -> Unit,
    onSetMode: (KanjiDropMode) -> Unit,
) {
    val kanji = phase.entries.getOrNull(phase.currentIndex) ?: return
    val correctAnswer = when (mode) {
        KanjiDropMode.HIRAGANA -> kanji.hiragana
        KanjiDropMode.ROMAJI -> kanji.kunYomi.firstOrNull()?.takeIf { it.isNotBlank() } ?: kanji.hiragana
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header row: score + lives + mode toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Score: ${phase.score}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { i ->
                    Text(
                        text = if (i < phase.lives) "❤️" else "🖤",
                        fontSize = 18.sp,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(
                    selected = mode == KanjiDropMode.HIRAGANA,
                    onClick = { onSetMode(KanjiDropMode.HIRAGANA) },
                    label = { Text("かな", fontSize = 11.sp) },
                )
                FilterChip(
                    selected = mode == KanjiDropMode.ROMAJI,
                    onClick = { onSetMode(KanjiDropMode.ROMAJI) },
                    label = { Text("ABC", fontSize = 11.sp) },
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        ProgressBar(phase.currentIndex, phase.entries.size)
        Spacer(Modifier.height(8.dp))

        // Falling kanji zone
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        ) {
            // Kanji falls from top (0%) to bottom (100%) based on dropProgress
            val dropFraction = if (phase.selectedOption != null) 1f else phase.dropProgress
            val bgColor by animateColorAsState(
                targetValue = when {
                    phase.selectedOption == null -> MaterialTheme.colorScheme.primaryContainer
                    phase.selectedOption == correctAnswer -> Color(0xFF4CAF50)
                    else -> MaterialTheme.colorScheme.error
                },
                label = "kanji_bg",
            )
            // Danger line at the drop limit — must match the max kanji offset (200.dp)
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .offset(y = 200.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                thickness = 2.dp,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .align(Alignment.TopCenter)
                    .offset(y = (dropFraction * 200).dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = kanji.kanji,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            if (phase.selectedOption != null) {
                Text(
                    text = correctAnswer,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (phase.selectedOption == correctAnswer) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // 2×2 answer options grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(phase.options) { _, option ->
                val bg by animateColorAsState(
                    targetValue = when {
                        phase.selectedOption == null -> MaterialTheme.colorScheme.surfaceVariant
                        option == correctAnswer -> Color(0xFF4CAF50)
                        option == phase.selectedOption -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    label = "opt_$option",
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .clickable(enabled = phase.selectedOption == null) { onSelectAnswer(option) }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = option,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun KanjiDropResultsContent(
    phase: KanjiDropPhase.Results,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "${phase.score} / ${phase.total}",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text("Play Again") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Games") }
    }
}

// ── Kanji Builder game screen ──────────────────────────────────────────────────

@Composable
fun KanjiBuilderGameScreen(jlptFilter: String = "all", onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: KanjiBuilderViewModel = viewModel(
        key = "builder_$jlptFilter",
        factory = viewModelFactory {
            initializer {
                KanjiBuilderViewModel(
                    kanjiRepository = container.kanjiRepository,
                    radicalRepository = container.radicalRepository,
                    jlptFilter = jlptFilter,
                )
            }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { KotobaTopBar(title = "Kanji Builder", onBack = onBack) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val phase = state.phase) {
                is KanjiBuilderPhase.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is KanjiBuilderPhase.Building -> KanjiBuilderContent(
                    phase = phase,
                    onTapTile = { vm.dispatchAction(KanjiBuilderAction.TapTile(it)) },
                )

                is KanjiBuilderPhase.Results -> KanjiBuilderResultsContent(
                    phase = phase,
                    onRestart = { vm.dispatchAction(KanjiBuilderAction.Restart) },
                    onBack = onBack,
                )
            }
        }
    }
}

@Composable
private fun KanjiBuilderContent(
    phase: KanjiBuilderPhase.Building,
    onTapTile: (String) -> Unit,
) {
    val kanji = phase.entries.getOrNull(phase.currentIndex) ?: return

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProgressBar(phase.currentIndex, phase.entries.size)

        // Kanji display card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = kanji.kanji,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = kanji.meaning,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tap all radicals that form this kanji",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                )
                // Show progress: X / N selected
                Text(
                    text = "${phase.selectedIds.size} / ${phase.correctIds.size} found",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                )
            }
        }

        // 3×3 radical tile grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(phase.tiles) { _, tile ->
                val isSelected = phase.selectedIds.contains(tile.radicalId)
                val isWrong = phase.wrongIds.contains(tile.radicalId)
                val bg by animateColorAsState(
                    targetValue = when {
                        isSelected -> Color(0xFF4CAF50)
                        isWrong -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    label = "builder_${tile.radicalId}",
                )
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .clickable(enabled = !isSelected && !isWrong) { onTapTile(tile.radicalId) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = tile.character,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected || isWrong) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = tile.meaning,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            color = if (isSelected || isWrong) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }

        Text(
            text = "Score: ${phase.score}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.End),
        )
    }
}

@Composable
private fun KanjiBuilderResultsContent(
    phase: KanjiBuilderPhase.Results,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "${phase.score} / ${phase.total}",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text("Play Again") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Games") }
    }
}

// ── Shared ────────────────────────────────────────────────────────────────────

@Composable
private fun ProgressBar(current: Int, total: Int) {
    Column {
        LinearProgressIndicator(
            progress = { if (total > 0) (current + 1).toFloat() / total else 1f },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "${current + 1} / $total",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.End).padding(end = 4.dp, top = 2.dp),
        )
    }
}
