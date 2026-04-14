package com.example.personalproject.jlpt

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.ui.components.KotobaTopBar

@Composable
fun JlptLevelScreen(
    level: String,
    onBack: () -> Unit,
    onVocab: (level: String) -> Unit,
    onKanji: (level: String) -> Unit,
    onGrammar: (level: String) -> Unit,
    onPhrases: (level: String) -> Unit,
    onPracticeTest: (level: String) -> Unit,
) {
    val container = LocalAppContainer.current

    data class SectionInfo(
        val title: String,
        val japanese: String,
        val known: Int,
        val total: Int,
        val onClick: () -> Unit,
    )

    val vocabData by produceState(Pair(0, 0)) {
        val all = container.vocabularyRepository.getAllWords().filter { it.jlptLevel == level }
        value = Pair(all.size, all.size)
    }
    val kanjiData by produceState(Pair(0, 0)) {
        val all = container.kanjiRepository.getAllKanji().filter { it.jlptLevel == level }
        value = Pair(all.size, all.size)
    }
    val grammarData by produceState(Pair(0, 0)) {
        val all = container.grammarRepository.getAllGrammar().filter { it.jlptLevel == level }
        value = Pair(all.size, all.size)
    }
    val phraseData by produceState(Pair(0, 0)) {
        val all = container.phraseRepository.getAllPhrases().filter { it.jlptLevel == level }
        value = Pair(all.size, all.size)
    }

    // Known counts from Room
    val knownVocab by container.knownRepository.getKnownCount("vocab").collectAsStateWithLifecycle(0)
    val knownKanji by container.knownRepository.getKnownCount("kanji").collectAsStateWithLifecycle(0)
    val knownGrammar by container.knownRepository.getKnownCount("grammar").collectAsStateWithLifecycle(0)
    val knownPhrase by container.knownRepository.getKnownCount("phrase").collectAsStateWithLifecycle(0)

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = "$level Study Hub", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // Practice Test button
            Button(
                onClick = { onPracticeTest(level) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    Icons.Filled.Quiz,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Practice Test",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = "Browse $level content",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
            )

            val sections = listOf(
                SectionInfo("Vocabulary", "語彙", knownVocab.coerceAtMost(vocabData.first), vocabData.first, { onVocab(level) }),
                SectionInfo("Kanji", "漢字", knownKanji.coerceAtMost(kanjiData.first), kanjiData.first, { onKanji(level) }),
                SectionInfo("Grammar", "文法", knownGrammar.coerceAtMost(grammarData.first), grammarData.first, { onGrammar(level) }),
                SectionInfo("Phrases", "フレーズ", knownPhrase.coerceAtMost(phraseData.first), phraseData.first, { onPhrases(level) }),
            )

            sections.forEach { section ->
                JlptSectionCard(section.title, section.japanese, section.known, section.total, section.onClick)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun JlptSectionCard(
    title: String,
    japanese: String,
    known: Int,
    total: Int,
    onClick: () -> Unit,
) {
    val progress = if (total > 0) known.toFloat() / total.toFloat() else 0f
    val pct = if (total > 0) (progress * 100).toInt() else 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = japanese,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Light,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (total > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$total items · $pct% known",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                } else {
                    Text(
                        text = "No $title content for this level",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            }
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
