package com.example.personalproject.jlpt

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.ui.components.KotobaTopBar
import kotlinx.coroutines.delay

private data class TestQuestion(
    val prompt: String,           // Japanese word/phrase
    val promptReading: String,    // hiragana
    val correctAnswer: String,    // English meaning
    val wrongAnswers: List<String>,
)

@Composable
fun JlptPracticeTestScreen(level: String, onBack: () -> Unit) {
    val container = LocalAppContainer.current

    val questions by produceState<List<TestQuestion>?>(null) {
        val vocab = container.vocabularyRepository.getAllWords().filter { it.jlptLevel == level }
        val nouns = container.nounRepository.getAllNouns().filter { it.jlptLevel == level }

        // Build pool from vocab + nouns (max 20 questions)
        val combined = (vocab.map { Triple(it.japanese, it.hiragana, it.english) } +
            nouns.map { Triple(it.kanji.ifBlank { it.hiragana }, it.hiragana, it.meaning) })
            .filter { it.third.isNotBlank() }
            .shuffled()
            .take(20)

        if (combined.isEmpty()) {
            value = emptyList()
            return@produceState
        }

        val allMeanings = combined.map { it.third }.distinct()

        value = combined.map { (jp, reading, correct) ->
            val distractors = allMeanings
                .filter { it != correct }
                .shuffled()
                .take(3)
            TestQuestion(
                prompt = jp,
                promptReading = reading,
                correctAnswer = correct,
                wrongAnswers = distractors,
            )
        }
    }

    when {
        questions == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Preparing test…", style = MaterialTheme.typography.bodyMedium)
            }
        }

        questions!!.isEmpty() -> Column(Modifier.fillMaxSize()) {
            KotobaTopBar(title = "$level Practice Test", onBack = onBack)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No content available for $level yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
            }
        }

        else -> TestSession(level = level, questions = questions!!, onBack = onBack)
    }
}

@Composable
private fun TestSession(level: String, questions: List<TestQuestion>, onBack: () -> Unit) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isFinished by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(60 * questions.size) } // 1 min per question

    LaunchedEffect(isFinished) {
        if (!isFinished) {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            isFinished = true
        }
    }

    if (isFinished) {
        TestResults(level = level, score = score, total = questions.size, onBack = onBack)
        return
    }

    val question = questions[currentIndex]
    val options = remember(currentIndex) {
        (listOf(question.correctAnswer) + question.wrongAnswers).shuffled()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "$level Practice Test",
            onBack = onBack,
            actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp),
                ) {
                    Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = "${timeLeft / 60}:${(timeLeft % 60).toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (timeLeft < 30) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Question ${currentIndex + 1} of ${questions.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            LinearProgressIndicator(
                progress = { (currentIndex.toFloat()) / questions.size },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
            )

            // Prompt card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = question.prompt,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )
                    if (question.promptReading.isNotBlank() && question.promptReading != question.prompt) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = question.promptReading,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            Text(
                text = "What does this mean?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            // Answer options
            options.forEach { option ->
                val isSelected = selectedAnswer == option
                val isCorrect = option == question.correctAnswer
                val bgColor = when {
                    selectedAnswer == null -> MaterialTheme.colorScheme.surfaceVariant
                    isCorrect -> Color(0xFF2E7D32).copy(alpha = 0.2f)
                    isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
                val borderColor = when {
                    selectedAnswer == null -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    isCorrect -> Color(0xFF2E7D32)
                    isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable(enabled = selectedAnswer == null) {
                            selectedAnswer = option
                            if (isCorrect) score++
                        }
                        .padding(16.dp),
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Next button (shown after selection)
            if (selectedAnswer != null) {
                Button(
                    onClick = {
                        if (currentIndex < questions.size - 1) {
                            currentIndex++
                            selectedAnswer = null
                        } else {
                            isFinished = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = if (currentIndex < questions.size - 1) "Next Question" else "See Results",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TestResults(level: String, score: Int, total: Int, onBack: () -> Unit) {
    val pct = if (total > 0) (score.toFloat() / total * 100).toInt() else 0
    val passed = pct >= 60
    val emoji = when {
        pct >= 90 -> "🏆"
        pct >= 70 -> "🎉"
        pct >= 60 -> "👍"
        else -> "📚"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = "$level Results", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = emoji, fontSize = 72.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (passed) "Well done!" else "Keep practising!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$score / $total correct ($pct%)",
                style = MaterialTheme.typography.titleLarge,
                color = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(40.dp),
                    )
                    Text(
                        text = if (passed) "You passed the $level practice test!" else "A passing score is 60%. Review $level content and try again.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Back to $level Hub", fontWeight = FontWeight.Bold)
            }
        }
    }
}
