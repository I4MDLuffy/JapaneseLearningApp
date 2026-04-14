package app.kotori.japanese.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kotori.japanese.data.kana.hiraganaGroups
import app.kotori.japanese.data.kana.katakanaGroups
import app.kotori.japanese.ui.components.KotobaTopBar

private data class DrawnPath(val points: List<Offset>)

private sealed class WritingState {
    data object Drawing : WritingState()
    data object Revealed : WritingState()
    data object Finished : WritingState()
}

@Composable
fun KanaWritingGameScreen(
    kanaType: String,
    onBack: () -> Unit,
) {
    val allKana = remember(kanaType) {
        val groups = when (kanaType) {
            "hiragana" -> hiraganaGroups
            "katakana" -> katakanaGroups
            else -> hiraganaGroups + katakanaGroups
        }
        groups.flatMap { it.entries }.shuffled()
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var state by remember { mutableStateOf<WritingState>(WritingState.Drawing) }
    var drawnPaths by remember { mutableStateOf(listOf<DrawnPath>()) }
    var currentPathPoints by remember { mutableStateOf(listOf<Offset>()) }

    if (state is WritingState.Finished || allKana.isEmpty()) {
        FinishedScreen(
            correct = correctCount,
            total = allKana.size,
            onBack = onBack,
            onRestart = {
                currentIndex = 0
                correctCount = 0
                state = WritingState.Drawing
                drawnPaths = emptyList()
                currentPathPoints = emptyList()
            },
        )
        return
    }

    val entry = allKana[currentIndex]
    val progress = currentIndex.toFloat() / allKana.size
    val isDrawing = state is WritingState.Drawing

    fun advanceToNext(correct: Boolean) {
        if (correct) correctCount++
        drawnPaths = emptyList()
        currentPathPoints = emptyList()
        if (currentIndex + 1 >= allKana.size) {
            state = WritingState.Finished
        } else {
            currentIndex++
            state = WritingState.Drawing
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = "Kana Writing", onBack = onBack)

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "${currentIndex + 1} / ${allKana.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            )

            Text(
                text = if (isDrawing) "Trace the character" else "How did you do?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )

            // Canvas / drawing area
            val strokeColor = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                // Ghost character — tracing template
                Text(
                    text = entry.kana,
                    fontSize = 160.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(isDrawing, entry) {
                            if (!isDrawing) return@pointerInput
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                currentPathPoints = listOf(down.position)
                                do {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull() ?: break
                                    change.consume()
                                    currentPathPoints = currentPathPoints + change.position
                                } while (event.changes.any { it.pressed })
                                if (currentPathPoints.size > 1) {
                                    drawnPaths = drawnPaths + DrawnPath(currentPathPoints)
                                }
                                currentPathPoints = emptyList()
                            }
                        },
                ) {
                    val style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    drawnPaths.forEach { drawn ->
                        if (drawn.points.size > 1) {
                            val path = Path().apply {
                                moveTo(drawn.points[0].x, drawn.points[0].y)
                                drawn.points.drop(1).forEach { lineTo(it.x, it.y) }
                            }
                            drawPath(path, strokeColor, style = style)
                        }
                    }
                    if (currentPathPoints.size > 1) {
                        val path = Path().apply {
                            moveTo(currentPathPoints[0].x, currentPathPoints[0].y)
                            currentPathPoints.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(path, strokeColor, style = style)
                    }
                }

                // Clear strokes button (top-right corner)
                if (drawnPaths.isNotEmpty() && isDrawing) {
                    IconButton(
                        onClick = { drawnPaths = emptyList(); currentPathPoints = emptyList() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp),
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }

                // Romaji reveal banner (bottom of canvas)
                if (state is WritingState.Revealed) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f))
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = entry.romaji,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            // Action buttons
            when (state) {
                is WritingState.Drawing -> {
                    Button(
                        onClick = { state = WritingState.Revealed },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Reveal Answer")
                    }
                }
                is WritingState.Revealed -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { advanceToNext(correct = false) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Need Practice")
                        }
                        Button(
                            onClick = { advanceToNext(correct = true) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Got it!")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun FinishedScreen(
    correct: Int,
    total: Int,
    onBack: () -> Unit,
    onRestart: () -> Unit,
) {
    val pct = if (total > 0) (correct * 100) / total else 0
    val emoji = when {
        pct >= 90 -> "🌟"
        pct >= 70 -> "👍"
        pct >= 50 -> "📝"
        else -> "💪"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = emoji, fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Practice Complete!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$correct / $total correct ($pct%)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text("Practice Again")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Games")
        }
    }
}
