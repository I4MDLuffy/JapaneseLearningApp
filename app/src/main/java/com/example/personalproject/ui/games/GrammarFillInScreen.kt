package com.example.personalproject.ui.games

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.data.model.GrammarEntry
import com.example.personalproject.ui.components.KotobaTopBar

private data class FillInQuestion(
    val entry: GrammarEntry,
    val sentence: String,       // full sentence
    val blank: String,          // the word/pattern to guess
    val display: String,        // sentence with "___" in place of blank
    val options: List<String>,
)

private fun buildFillInQuestion(entry: GrammarEntry, allEntries: List<GrammarEntry>): FillInQuestion? {
    // Use exampleOne as sentence. The "title" is typically the grammar keyword.
    val sentence = entry.exampleOne.trim()
    val keyword = entry.title.trim()
    if (sentence.isBlank() || keyword.isBlank()) return null
    if (!sentence.contains(keyword)) return null

    val display = sentence.replace(keyword, "＿＿＿")

    val distractors = allEntries
        .filter { it.id != entry.id && it.title.isNotBlank() }
        .shuffled()
        .take(3)
        .map { it.title }

    if (distractors.size < 3) return null

    return FillInQuestion(
        entry = entry,
        sentence = sentence,
        blank = keyword,
        display = display,
        options = (listOf(keyword) + distractors).shuffled(),
    )
}

@Composable
fun GrammarFillInScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current

    val questions by produceState<List<FillInQuestion>?>(null) {
        val all = container.grammarRepository.getAllGrammar()
        value = all.shuffled()
            .mapNotNull { buildFillInQuestion(it, all) }
            .take(20)
    }

    when {
        questions == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        questions!!.isEmpty() -> Column(Modifier.fillMaxSize()) {
            KotobaTopBar(title = "Fill-in-Blank", onBack = onBack)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Not enough grammar examples to generate questions.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp))
            }
        }
        else -> FillInSession(questions = questions!!, onBack = onBack)
    }
}

@Composable
private fun FillInSession(questions: List<FillInQuestion>, onBack: () -> Unit) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    var isFinished by remember { mutableStateOf(false) }

    if (isFinished) {
        val pct = if (questions.isNotEmpty()) (score.toFloat() / questions.size * 100).toInt() else 0
        Column(Modifier.fillMaxSize()) {
            KotobaTopBar(title = "Fill-in — Results", onBack = onBack)
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(if (pct >= 70) "🎉" else "📚", fontSize = 72.sp)
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
        KotobaTopBar(title = "Fill-in-Blank", onBack = onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${currentIndex + 1} / ${questions.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                Text("Score: $score", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            LinearProgressIndicator(progress = { currentIndex.toFloat() / questions.size }, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)))

            // Sentence prompt
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Show with blank
                    val displayText = buildAnnotatedString {
                        val parts = q.display.split("＿＿＿")
                        parts.forEachIndexed { i, part ->
                            append(part)
                            if (i < parts.size - 1) {
                                if (selected != null) {
                                    // Show the answer
                                    val color = if (selected == q.blank) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color)) {
                                        append(q.blank)
                                    }
                                } else {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)) {
                                        append("＿＿＿")
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )

                    // Grammar point label
                    Box(
                        Modifier.clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)).padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(q.entry.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                }
            }

            Text("Fill in the blank:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

            q.options.forEach { option ->
                val isChosen = selected == option
                val isCorrect = option == q.blank
                val bg = when {
                    selected == null -> MaterialTheme.colorScheme.surfaceVariant
                    isCorrect -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                    isChosen -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
                val borderColor = when {
                    selected == null -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    isCorrect -> Color(0xFF2E7D32)
                    isChosen -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                }
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bg).border(1.dp, borderColor, RoundedCornerShape(12.dp)).clickable(enabled = selected == null) {
                        selected = option
                        if (option == q.blank) score++
                    }.padding(16.dp)
                ) {
                    Text(option, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }

            // Explanation after answering
            if (selected != null) {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.secondaryContainer).padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Grammar: ${q.entry.title}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        if (q.entry.content.isNotBlank()) {
                            Text(q.entry.content.take(150) + if (q.entry.content.length > 150) "…" else "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                        }
                    }
                }

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
