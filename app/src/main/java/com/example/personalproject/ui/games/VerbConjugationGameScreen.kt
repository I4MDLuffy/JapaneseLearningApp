package app.kotori.japanese.ui.games

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.data.model.VerbEntry
import app.kotori.japanese.ui.components.KotobaTopBar

private data class ConjQuestion(
    val verb: VerbEntry,
    val formKey: String,
    val formLabel: String,
    val correctAnswer: String,
    val options: List<String>,
)

private fun getFormValue(verb: VerbEntry, key: String): String = when (key) {
    "presentAffirmative"   -> verb.presentAffirmative
    "presentNegative"      -> verb.presentNegative
    "pastAffirmative"      -> verb.pastAffirmative
    "pastNegative"         -> verb.pastNegative
    "teFormAffirmative"    -> verb.teFormAffirmative
    "presentShortNegative" -> verb.presentShortNegative
    "pastShortAffirmative" -> verb.pastShortAffirmative
    "pastShortNegative"    -> verb.pastShortNegative
    "tai"                  -> verb.tai
    "volitional"           -> verb.volitional
    "potential"            -> verb.potential
    "passive"              -> verb.passive
    "causative"            -> verb.causative
    else -> ""
}

@Composable
fun VerbConjugationGameScreen(
    level: String,
    formKeys: String,
    count: Int,
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val forms = formKeys.split(",").filter { it.isNotBlank() }
    val formLabels = verbConjugationForms.toMap()

    val questions by produceState<List<ConjQuestion>?>(null) {
        val allVerbs = container.verbRepository.getAllVerbs()
        val filtered = if (level == "All") allVerbs else allVerbs.filter { it.jlptLevel == level }
        val picked = filtered.shuffled().take(count)

        if (picked.isEmpty()) {
            value = emptyList()
            return@produceState
        }

        // Build all possible answers for distractors
        val allAnswers = allVerbs.flatMap { v -> forms.map { f -> getFormValue(v, f) } }
            .filter { it.isNotBlank() }
            .distinct()

        value = picked.flatMap { verb ->
            forms.mapNotNull { formKey ->
                val correct = getFormValue(verb, formKey)
                if (correct.isBlank()) return@mapNotNull null
                val distractors = allAnswers
                    .filter { it != correct }
                    .shuffled()
                    .take(3)
                if (distractors.size < 3) return@mapNotNull null
                ConjQuestion(
                    verb = verb,
                    formKey = formKey,
                    formLabel = formLabels[formKey] ?: formKey,
                    correctAnswer = correct,
                    options = (listOf(correct) + distractors).shuffled(),
                )
            }
        }.shuffled().take(count * forms.size)
    }

    when {
        questions == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        questions!!.isEmpty() -> Column(Modifier.fillMaxSize()) {
            KotobaTopBar(title = "Verb Drill", onBack = onBack)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No verbs found for $level. Try 'All'.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }
        }
        else -> ConjDrillSession(questions = questions!!, onBack = onBack)
    }
}

@Composable
private fun ConjDrillSession(questions: List<ConjQuestion>, onBack: () -> Unit) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    var isFinished by remember { mutableStateOf(false) }

    if (isFinished) {
        val pct = if (questions.isNotEmpty()) (score.toFloat() / questions.size * 100).toInt() else 0
        Column(Modifier.fillMaxSize()) {
            KotobaTopBar(title = "Verb Drill — Results", onBack = onBack)
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = if (pct >= 70) "🎉" else "📚", fontSize = 72.sp)
                Spacer(Modifier.height(16.dp))
                Text("$score / ${questions.size} correct ($pct%)", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("Finish", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val q = questions[currentIndex]
    Column(Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Verb Drill",
            onBack = onBack,
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${currentIndex + 1} / ${questions.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                Text("Score: $score", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            LinearProgressIndicator(progress = { currentIndex.toFloat() / questions.size }, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)))

            // Prompt
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(q.verb.kanji.ifBlank { q.verb.dictionaryForm }, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
                    Text(q.verb.meaning, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(q.formLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("Choose the correct form:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

            q.options.forEach { option ->
                val isChosen = selected == option
                val isCorrect = option == q.correctAnswer
                val bg = when {
                    selected == null -> MaterialTheme.colorScheme.surfaceVariant
                    isCorrect -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                    isChosen -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
                val border = when {
                    selected == null -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    isCorrect -> Color(0xFF2E7D32)
                    isChosen -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                }
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bg).border(1.dp, border, RoundedCornerShape(12.dp)).clickable(enabled = selected == null) {
                        selected = option
                        if (isCorrect) score++
                    }.padding(16.dp)
                ) {
                    Text(option, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }

            if (selected != null) {
                Button(
                    onClick = {
                        if (currentIndex < questions.size - 1) {
                            currentIndex++
                            selected = null
                        } else {
                            isFinished = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(if (currentIndex < questions.size - 1) "Next" else "See Results", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
