package com.example.personalproject.verbs.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.data.model.VerbEntry
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.verbs.detail.mvi.VerbDetailViewModel

@Composable
fun VerbDetailScreen(verbId: String, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: VerbDetailViewModel = viewModel(
        key = verbId,
        factory = viewModelFactory {
            initializer { VerbDetailViewModel(container.verbRepository, verbId) }
        }
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { KotobaTopBar(title = state.entry?.meaning ?: "Verb", onBack = onBack) },
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.entry != null -> VerbDetail(entry = state.entry!!, modifier = Modifier.padding(padding))

            else -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { Text("Verb not found.") }
        }
    }
}

@Composable
private fun VerbDetail(entry: VerbEntry, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = entry.kanji.ifBlank { entry.dictionaryForm },
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
                if (entry.dictionaryForm.isNotBlank()) {
                    Text(
                        text = entry.dictionaryForm,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.romaji,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.meaning,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Info
            SectionCard(label = "Info") {
                ConjugationRow("Type", entry.verbType)
                if (entry.transitivity.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ConjugationRow("Transitivity", entry.transitivity)
                }
                if (entry.stem.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ConjugationRow("Stem", entry.stem)
                }
            }

            // Polite (long) forms
            SectionCard(label = "Polite Forms") {
                ConjugationRow("Present +", entry.presentAffirmative)
                Divider()
                ConjugationRow("Present –", entry.presentNegative)
                Divider()
                ConjugationRow("Past +", entry.pastAffirmative)
                Divider()
                ConjugationRow("Past –", entry.pastNegative)
            }

            // Te-form & short forms
            SectionCard(label = "Short & Te-forms") {
                ConjugationRow("Te-form", entry.teFormAffirmative)
                Divider()
                ConjugationRow("Present neg.", entry.presentShortNegative)
                Divider()
                ConjugationRow("Te-form (ないで)", entry.teFormNegativeNaide)
                Divider()
                ConjugationRow("Te-form (なくて)", entry.teFormNegativeNakute)
                Divider()
                ConjugationRow("Past short +", entry.pastShortAffirmative)
                Divider()
                ConjugationRow("Past short –", entry.pastShortNegative)
            }

            // Advanced forms
            SectionCard(label = "Advanced Forms") {
                ConjugationRow("Tai (want to)", entry.tai)
                Divider()
                ConjugationRow("Volitional", entry.volitional)
                Divider()
                ConjugationRow("Ba + ", entry.baFormAffirmative)
                Divider()
                ConjugationRow("Ba – ", entry.baFormNegative)
                Divider()
                ConjugationRow("Potential", entry.potential)
                Divider()
                ConjugationRow("Causative", entry.causative)
                Divider()
                ConjugationRow("Passive", entry.passive)
                Divider()
                ConjugationRow("Caus. passive", entry.causativePassive)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 6.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

@Composable
private fun ConjugationRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.6f),
        )
    }
}

@Composable
private fun SectionCard(label: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
