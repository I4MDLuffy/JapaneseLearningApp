package app.kotori.japanese.ui.basiccharacters

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.data.kana.hiraganaGroups
import app.kotori.japanese.data.kana.katakanaGroups
import app.kotori.japanese.ui.components.KotobaTopBar

@Composable
fun KanaGroupGameScreen(
    kanaType: String,
    groupId: String,
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val groups = if (kanaType == "hiragana") hiraganaGroups else katakanaGroups

    val entries = remember(kanaType, groupId) {
        when {
            groupId == "all" -> groups.flatMap { it.entries }
            groupId.contains(",") -> {
                val ids = groupId.split(",").toSet()
                groups.filter { it.id in ids }.flatMap { it.entries }
            }
            else -> groups.find { it.id == groupId }?.entries ?: emptyList()
        }
    }

    val groupLabel = remember(kanaType, groupId) {
        when {
            groupId == "all" -> "All ${kanaType.replaceFirstChar { it.uppercase() }}"
            groupId.contains(",") -> {
                val ids = groupId.split(",").toSet()
                val count = groups.filter { it.id in ids }.sumOf { it.entries.size }
                "Selected ($count characters)"
            }
            else -> groups.find { it.id == groupId }?.label ?: groupId
        }
    }

    val vm = viewModel<KanaGameViewModel>(
        factory = viewModelFactory {
            initializer {
                KanaGameViewModel(
                    allEntries = entries,
                    onAnswer = { kana, correct ->
                        container.kanaStatsRepository.recordAnswer(kana, kanaType, correct)
                    },
                )
            }
        },
    )

    val state by vm.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = groupLabel, onBack = onBack)

        when (val phase = state.phase) {
            is KanaGamePhase.ModeSelect ->
                ModeSelectContent(groupLabel, entries.size) { vm.dispatchAction(KanaGameAction.SelectMode(it)) }

            is KanaGamePhase.Matching ->
                MatchingContent(phase) { vm.dispatchAction(KanaGameAction.TapMatchCard(it)) }

            is KanaGamePhase.Typing ->
                TypingContent(
                    phase = phase,
                    onInput = { vm.dispatchAction(KanaGameAction.UpdateTypingInput(it)) },
                    onSubmit = { vm.dispatchAction(KanaGameAction.SubmitTyping) },
                )

            is KanaGamePhase.Flashcard ->
                FlashcardContent(
                    phase = phase,
                    onFlip = { vm.dispatchAction(KanaGameAction.FlipFlashcard) },
                    onAnswer = { vm.dispatchAction(KanaGameAction.AnswerFlashcard(it)) },
                )

            is KanaGamePhase.MultipleChoice ->
                MultipleChoiceContent(phase) { vm.dispatchAction(KanaGameAction.SelectChoice(it)) }

            is KanaGamePhase.KanaSpeed ->
                KanaSpeedContent(
                    phase = phase,
                    onInput = { vm.dispatchAction(KanaGameAction.UpdateSpeedInput(it)) },
                    onSubmit = { vm.dispatchAction(KanaGameAction.SubmitSpeed) },
                )

            is KanaGamePhase.Results ->
                ResultsContent(phase, onPlayAgain = { vm.dispatchAction(KanaGameAction.Restart) }, onBack = onBack)
        }
    }
}

// ── Mode select — fills screen with all 5 modes ───────────────────────────────

private data class ModeInfo(val mode: GameMode, val title: String, val description: String)

private val allModes = listOf(
    ModeInfo(GameMode.MATCHING,        "Match Pairs",      "Tap kana & romaji\nto find each pair"),
    ModeInfo(GameMode.FLASHCARD,       "Flashcard",        "Reveal the reading,\nmark correct or wrong"),
    ModeInfo(GameMode.TYPING,          "Type Romaji",      "See kana,\ntype the reading"),
    ModeInfo(GameMode.MULTIPLE_CHOICE, "Multiple Choice",  "Pick the correct\nromaji from 4 options"),
    ModeInfo(GameMode.KANA_SPEED,      "Kana Speed",       "6 seconds per kana —\ntype before time runs out"),
)

@Composable
private fun ModeSelectContent(
    groupLabel: String,
    entryCount: Int,
    onSelectMode: (GameMode) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Compact header
        Text(
            text = groupLabel,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "$entryCount characters  •  Choose a mode",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Modes grid — fills remaining space
        // Rows: [0,1] [2,3] [4 full-width]
        val rows = allModes.chunked(2)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rows.forEach { rowModes ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowModes.forEach { info ->
                        GameModeCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            title = info.title,
                            description = info.description,
                            onClick = { onSelectMode(info.mode) },
                        )
                    }
                    // Pad odd row so last card doesn't stretch weirdly
                    if (rowModes.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun GameModeCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Matching ──────────────────────────────────────────────────────────────────

@Composable
private fun MatchingContent(phase: KanaGamePhase.Matching, onCardTap: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Batch ${phase.batchIndex + 1} of ${phase.totalBatches}  •  Score: ${phase.totalScore}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { phase.batchIndex.toFloat() / phase.totalBatches.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Tap a kana then its romaji to match",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(phase.cards) { index, card ->
                MatchCardItem(
                    card = card,
                    isSelected = phase.firstSelectedIndex == index,
                    isMatched = phase.matchedIds.contains(card.pairId),
                    isWrong = phase.wrongIndices.contains(index),
                    onClick = { onCardTap(index) },
                )
            }
        }
    }
}

@Composable
private fun MatchCardItem(
    card: MatchCard,
    isSelected: Boolean,
    isMatched: Boolean,
    isWrong: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isMatched  -> Color(0xFF4CAF50)
            isWrong    -> Color(0xFFF44336)
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else       -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(200),
        label = "match_card_bg",
    )
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)) else Modifier)
            .clickable(enabled = !isMatched) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = card.text,
            fontSize = if (card.isKana) 22.sp else 12.sp,
            fontWeight = FontWeight.Medium,
            color = when {
                isMatched || isWrong -> Color.White
                isSelected           -> MaterialTheme.colorScheme.onPrimaryContainer
                else                 -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(4.dp),
        )
    }
}

// ── Typing ────────────────────────────────────────────────────────────────────

@Composable
private fun TypingContent(
    phase: KanaGamePhase.Typing,
    onInput: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val progress = (phase.currentIndex.toFloat() / phase.entries.size.toFloat()).coerceIn(0f, 1f)
    val currentEntry = phase.entries[phase.currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            Text("${phase.currentIndex + 1} / ${phase.entries.size}", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Score: ${phase.score}", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.End))
        Spacer(modifier = Modifier.weight(1f))
        KanaDisplay(text = currentEntry.kana)
        Spacer(modifier = Modifier.height(16.dp))
        FeedbackText(phase.feedback, currentEntry.romaji)
        Spacer(modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = phase.inputText,
            onValueChange = onInput,
            placeholder = { Text("Type romaji…") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = phase.feedback == null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = phase.inputText.isNotBlank() && phase.feedback == null,
        ) { Text("Check") }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ── Flashcard ─────────────────────────────────────────────────────────────────

@Composable
private fun FlashcardContent(
    phase: KanaGamePhase.Flashcard,
    onFlip: () -> Unit,
    onAnswer: (Boolean) -> Unit,
) {
    val progress = (phase.currentIndex.toFloat() / phase.entries.size.toFloat()).coerceIn(0f, 1f)
    val entry = phase.entries[phase.currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            Text("${phase.currentIndex + 1} / ${phase.entries.size}", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text("✓ ${phase.correctCount}", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Text("✗ ${phase.incorrectCount}", color = Color(0xFFF44336), style = MaterialTheme.typography.labelMedium)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .then(if (!phase.revealed) Modifier.clickable { onFlip() } else Modifier)
                .padding(vertical = 48.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = entry.kana,
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (phase.revealed) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = entry.romaji,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tap to reveal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (phase.revealed) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    onClick = { onAnswer(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                ) { Text("Missed") }
                Button(
                    onClick = { onAnswer(true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                ) { Text("Got it") }
            }
        } else {
            Button(
                onClick = onFlip,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Reveal") }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ── Multiple choice ───────────────────────────────────────────────────────────

@Composable
private fun MultipleChoiceContent(
    phase: KanaGamePhase.MultipleChoice,
    onSelect: (String) -> Unit,
) {
    val progress = (phase.currentIndex.toFloat() / phase.entries.size.toFloat()).coerceIn(0f, 1f)
    val entry = phase.entries[phase.currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            Text("${phase.currentIndex + 1} / ${phase.entries.size}", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Score: ${phase.score}", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.End))
        Spacer(modifier = Modifier.weight(1f))
        KanaDisplay(text = entry.kana)
        Spacer(modifier = Modifier.weight(1f))

        // 2×2 option grid
        val optionRows = phase.options.chunked(2)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            optionRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    row.forEach { option ->
                        val isSelected = phase.selectedOption == option
                        val isCorrect = phase.isCorrect
                        val containerColor = when {
                            isSelected && isCorrect == true  -> Color(0xFF4CAF50)
                            isSelected && isCorrect == false -> Color(0xFFF44336)
                            !isSelected && phase.selectedOption != null && option == entry.romaji -> Color(0xFF4CAF50)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(containerColor)
                                .clickable(enabled = phase.selectedOption == null) { onSelect(option) }
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected || (phase.selectedOption != null && option == entry.romaji))
                                    Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Kana speed ────────────────────────────────────────────────────────────────

@Composable
private fun KanaSpeedContent(
    phase: KanaGamePhase.KanaSpeed,
    onInput: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val progress = (phase.currentIndex.toFloat() / phase.entries.size.toFloat()).coerceIn(0f, 1f)
    val entry = phase.entries[phase.currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            Text("${phase.currentIndex + 1} / ${phase.entries.size}", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Score: ${phase.score}", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.End))
        Spacer(modifier = Modifier.height(8.dp))

        // Timer bar
        val timerColor by animateColorAsState(
            targetValue = when {
                phase.timeRemaining > 0.5f -> Color(0xFF4CAF50)
                phase.timeRemaining > 0.2f -> Color(0xFFFFC107)
                else                       -> Color(0xFFF44336)
            },
            animationSpec = tween(200),
            label = "timer_color",
        )
        LinearProgressIndicator(
            progress = { phase.timeRemaining },
            modifier = Modifier.fillMaxWidth(),
            color = timerColor,
        )

        Spacer(modifier = Modifier.weight(1f))
        KanaDisplay(text = entry.kana)
        Spacer(modifier = Modifier.height(16.dp))
        FeedbackText(phase.feedback, entry.romaji)
        Spacer(modifier = Modifier.weight(1f))

        OutlinedTextField(
            value = phase.inputText,
            onValueChange = onInput,
            placeholder = { Text("Type romaji fast!") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = phase.feedback == null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = phase.inputText.isNotBlank() && phase.feedback == null,
        ) { Text("Submit") }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ── Results ───────────────────────────────────────────────────────────────────

@Composable
private fun ResultsContent(
    phase: KanaGamePhase.Results,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit,
) {
    val percentage = if (phase.total > 0) (phase.score * 100 / phase.total) else 0
    val isMastery = percentage == 100

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (isMastery) "Perfect!" else "Results",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = if (isMastery) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("${phase.score} / ${phase.total}", fontSize = 56.sp, fontWeight = FontWeight.Bold)
        Text("$percentage%", style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        if (isMastery) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Mastery achieved!", style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) { Text("Play Again") }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Table") }
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
private fun KanaDisplay(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 48.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, fontSize = 80.sp, fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FeedbackText(feedback: TypingFeedback?, correctAnswer: String) {
    val text = when (feedback) {
        TypingFeedback.CORRECT -> "Correct!"
        TypingFeedback.WRONG   -> "Incorrect — $correctAnswer"
        null                   -> ""
    }
    val color = when (feedback) {
        TypingFeedback.CORRECT -> Color(0xFF4CAF50)
        TypingFeedback.WRONG   -> Color(0xFFF44336)
        null                   -> Color.Transparent
    }
    Text(text = text, style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold, color = color,
        modifier = Modifier.height(28.dp))
}
