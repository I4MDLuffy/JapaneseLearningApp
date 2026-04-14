package app.kotori.japanese.radicals

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.data.model.RadicalEntry
import app.kotori.japanese.ui.components.ItemNavigationBar
import app.kotori.japanese.ui.components.KotobaTopBar
import app.kotori.japanese.util.swipeToNavigate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RadicalDetailScreen(
    radicalId: String,
    onBack: () -> Unit,
    onViewKanji: (radicalId: String) -> Unit,
    onKanjiClick: (kanjiId: String) -> Unit,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
) {
    val container = LocalAppContainer.current
    val radical by produceState<RadicalEntry?>(null) {
        value = container.radicalRepository.getRadicalById(radicalId)
    }
    val isKnown by container.knownRepository.isItemKnownFlow("radical", radicalId)
        .collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            KotobaTopBar(
                title = radical?.meaning ?: "Radical",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        scope.launch { container.knownRepository.toggle("radical", radicalId) }
                    }) {
                        Icon(
                            imageVector = if (isKnown) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (isKnown) "Mark as unknown" else "Mark as known",
                            tint = if (isKnown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (onPrevious != null || onNext != null) {
                ItemNavigationBar(onPrevious = onPrevious, onNext = onNext)
            }
        },
    ) { padding ->
        when {
            radical == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> RadicalDetail(
                radical = radical!!,
                onViewKanji = { onViewKanji(radicalId) },
                onKanjiClick = onKanjiClick,
                modifier = Modifier
                    .padding(padding)
                    .swipeToNavigate(onSwipeLeft = onNext, onSwipeRight = onPrevious),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RadicalDetail(
    radical: RadicalEntry,
    onViewKanji: () -> Unit,
    onKanjiClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        // Hero
        Box(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = radical.character,
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
                if (radical.variantForms.isNotEmpty()) {
                    Text(
                        text = "Variant: ${radical.variantForms.joinToString(" ")}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = radical.meaning,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Properties
            SectionCard(label = "Properties") {
                PropRow("Stroke count", radical.strokeCount.toString())
                if (radical.position.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    PropRow("Position", radical.position.replaceFirstChar { it.uppercase() })
                }
                Spacer(modifier = Modifier.height(6.dp))
                PropRow("Frequency", radical.frequency.replaceFirstChar { it.uppercase() })
            }

            // Example kanji
            if (radical.exampleKanji.isNotEmpty()) {
                SectionCard(label = "Example Kanji") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        radical.exampleKanji.forEach { kanji ->
                            Text(
                                text = kanji,
                                fontSize = 28.sp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            // View all kanji using this radical
            OutlinedButton(
                onClick = onViewKanji,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("View All Kanji Using 「${radical.character}」")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PropRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.45f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.55f))
    }
}

@Composable
private fun SectionCard(label: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
