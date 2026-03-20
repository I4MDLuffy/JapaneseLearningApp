package com.example.personalproject.beginner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personalproject.ui.components.KotobaTopBar

private data class BeginnerSection(
    val emoji: String,
    val title: String,
    val description: String,
    val onClick: () -> Unit,
)

@Composable
fun BeginnerScreen(
    onBack: () -> Unit,
    onHiraganaKatakana: () -> Unit,
    onGrammar: () -> Unit,
    onNouns: () -> Unit,
    onVerbs: () -> Unit,
    onAdjectives: () -> Unit,
    onPhrases: () -> Unit,
    onKanji: () -> Unit,
) {
    val sections = listOf(
        BeginnerSection("あ", "Hiragana & Katakana", "Master the two Japanese syllabaries", onHiraganaKatakana),
        BeginnerSection("📖", "Grammar", "Basic sentence structure and particles", onGrammar),
        BeginnerSection("🏷️", "Nouns", "Everyday vocabulary by topic", onNouns),
        BeginnerSection("🏃", "Verbs", "Action words with all conjugations", onVerbs),
        BeginnerSection("✨", "Adjectives", "い and な adjectives", onAdjectives),
        BeginnerSection("💬", "Phrases", "Useful expressions and set phrases", onPhrases),
        BeginnerSection("漢", "Most Frequent Kanji", "Top kanji for N5 learners", onKanji),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = "Beginner  🌱", onBack = onBack)

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "N5 Level",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            items(sections) { section ->
                BeginnerSectionCard(section)
            }
        }
    }
}

@Composable
private fun BeginnerSectionCard(section: BeginnerSection) {
    Card(
        onClick = section.onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = section.emoji,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = section.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}
