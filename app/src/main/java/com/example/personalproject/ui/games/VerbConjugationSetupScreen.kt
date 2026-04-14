package app.kotori.japanese.ui.games

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kotori.japanese.ui.components.KotobaTopBar

/** Conjugation form key → display name */
val verbConjugationForms = listOf(
    "presentAffirmative"    to "Present Affirmative (〜ます)",
    "presentNegative"       to "Present Negative (〜ません)",
    "pastAffirmative"       to "Past Affirmative (〜ました)",
    "pastNegative"          to "Past Negative (〜ませんでした)",
    "teFormAffirmative"     to "Te-form (〜て/で)",
    "presentShortNegative"  to "Short Present Neg (〜ない)",
    "pastShortAffirmative"  to "Short Past (〜た/だ)",
    "pastShortNegative"     to "Short Past Neg (〜なかった)",
    "tai"                   to "Want-form (〜たい)",
    "volitional"            to "Volitional (〜よう)",
    "potential"             to "Potential (〜られる)",
    "passive"               to "Passive (〜られる)",
    "causative"             to "Causative (〜させる)",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VerbConjugationSetupScreen(
    onBack: () -> Unit,
    onStart: (level: String, formKeys: String, count: Int) -> Unit,
) {
    val jlptOptions = listOf("All", "N5", "N4", "N3", "N2", "N1")
    var selectedLevel by remember { mutableStateOf("All") }
    val selectedForms = remember { mutableStateOf(setOf("presentAffirmative", "pastAffirmative", "teFormAffirmative")) }
    var countSlider by remember { mutableFloatStateOf(10f) }
    val count = countSlider.toInt()

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = "Verb Drill Setup", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // JLPT Level
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("JLPT Level", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        jlptOptions.forEach { level ->
                            FilterChip(
                                selected = selectedLevel == level,
                                onClick = { selectedLevel = level },
                                label = { Text(level) },
                            )
                        }
                    }
                }
            }

            // Conjugation forms
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Conjugation Forms", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "${selectedForms.value.size} selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    )
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        verbConjugationForms.forEach { (key, label) ->
                            val isSelected = selectedForms.value.contains(key)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedForms.value = if (isSelected) {
                                        selectedForms.value - key
                                    } else {
                                        selectedForms.value + key
                                    }
                                },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            )
                        }
                    }
                }
            }

            // Verb count
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Number of Verbs", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Slider(
                        value = countSlider,
                        onValueChange = { countSlider = it },
                        valueRange = 5f..30f,
                        steps = 4,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Button(
                onClick = {
                    if (selectedForms.value.isNotEmpty()) {
                        onStart(selectedLevel, selectedForms.value.joinToString(","), count)
                    }
                },
                enabled = selectedForms.value.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Start Verb Drill", fontWeight = FontWeight.Bold)
            }
        }
    }
}
