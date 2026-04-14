package app.kotori.japanese.radicals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.data.model.RadicalEntry
import app.kotori.japanese.radicals.mvi.RadicalGameAction
import app.kotori.japanese.radicals.mvi.RadicalGamePhase
import app.kotori.japanese.radicals.mvi.RadicalGameViewModel
import app.kotori.japanese.ui.components.KotobaTopBar

@Composable
fun RadicalGameScreen(
    groupId: String,
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val vm: RadicalGameViewModel = viewModel(
        key = "radical_game_$groupId",
        factory = viewModelFactory {
            initializer {
                RadicalGameViewModel(groupId, container.radicalRepository)
            }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { KotobaTopBar(title = "Radical Study", onBack = onBack) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val phase = state.phase) {
                is RadicalGamePhase.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is RadicalGamePhase.Flashcard -> FlashcardContent(phase, vm)
                is RadicalGamePhase.MultipleChoice -> MultipleChoiceContent(phase, vm)
                is RadicalGamePhase.Results -> ResultsContent(phase.score, phase.total, vm)
            }
        }
    }
}

@Composable
private fun FlashcardContent(phase: RadicalGamePhase.Flashcard, vm: RadicalGameViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LinearProgressIndicator(
            progress = { (phase.currentIndex + 1f) / phase.entries.size },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "${phase.currentIndex + 1} / ${phase.entries.size}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.End),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = { vm.dispatchAction(RadicalGameAction.SwitchToMultipleChoice) }) {
                Text("Switch to Quiz")
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            onClick = { if (!phase.isRevealed) vm.dispatchAction(RadicalGameAction.FlipCard) },
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = phase.entries[phase.currentIndex].character,
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (phase.isRevealed) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = phase.entries[phase.currentIndex].meaning,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )
                    if (phase.entries[phase.currentIndex].variantForms.isNotEmpty()) {
                        Text(
                            text = "Variant: ${phase.entries[phase.currentIndex].variantForms.joinToString(" ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        )
                    }
                    Text(
                        text = "${phase.entries[phase.currentIndex].strokeCount} stroke${if (phase.entries[phase.currentIndex].strokeCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                    )
                } else {
                    Spacer(Modifier.height(16.dp))
                    Text("Tap to reveal", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f))
                }
            }
        }
        if (phase.isRevealed) {
            val isLast = phase.currentIndex >= phase.entries.size - 1
            if (isLast) {
                Button(onClick = { vm.dispatchAction(RadicalGameAction.NextCard) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Finish")
                }
            } else {
                Button(onClick = { vm.dispatchAction(RadicalGameAction.NextCard) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Next")
                }
            }
        }
    }
}

@Composable
private fun MultipleChoiceContent(phase: RadicalGamePhase.MultipleChoice, vm: RadicalGameViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LinearProgressIndicator(
            progress = { (phase.currentIndex + 1f) / phase.entries.size },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "${phase.currentIndex + 1} / ${phase.entries.size}  •  Score: ${phase.score}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.End),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = { vm.dispatchAction(RadicalGameAction.SwitchToFlashcard) }) {
                Text("Switch to Cards")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = phase.entries[phase.currentIndex].character,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Text("What does this radical mean?", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))

        phase.options.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { option ->
                    val bgColor = when {
                        phase.selectedOption == null -> MaterialTheme.colorScheme.surfaceVariant
                        option == phase.entries[phase.currentIndex].meaning -> MaterialTheme.colorScheme.primaryContainer
                        option == phase.selectedOption -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .clickable(enabled = phase.selectedOption == null) {
                                vm.dispatchAction(RadicalGameAction.SelectOption(option))
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = option, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
                    }
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ResultsContent(score: Int, total: Int, vm: RadicalGameViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Results", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Text("$score / $total", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("correct", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        Spacer(Modifier.height(32.dp))
        Button(onClick = { vm.dispatchAction(RadicalGameAction.Restart) }, modifier = Modifier.fillMaxWidth()) {
            Text("Play Again")
        }
    }
}
