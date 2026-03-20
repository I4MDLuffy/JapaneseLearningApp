package com.example.personalproject.gettingstarted

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personalproject.ui.components.KotobaTopBar

private data class GlossaryEntry(val term: String, val definition: String)

private val glossary = listOf(
    GlossaryEntry("Noun (名詞 • めいし)", "A word that names a person, place, thing, or idea. Example: 本 (ほん) = book."),
    GlossaryEntry("Verb (動詞 • どうし)", "A word that describes an action or state. Japanese verbs go at the end of a sentence. Example: たべる = to eat."),
    GlossaryEntry("い-Adjective (い形容詞)", "An adjective ending in い that conjugates by changing or dropping the い. Example: おおきい = big → おおきくない = not big."),
    GlossaryEntry("な-Adjective (な形容詞)", "An adjective that uses な before a noun and conjugates differently from い-adjectives. Example: しずかな = quiet."),
    GlossaryEntry("Particle (助詞 • じょし)", "A small word that marks the grammatical role of words in a sentence. Example: は marks the topic, を marks the object."),
    GlossaryEntry("Dictionary Form (辞書形 • じしょけい)", "The plain/base form of a verb as found in a dictionary. Example: たべる, いく, する."),
    GlossaryEntry("Polite Form (丁寧形 • ていねいけい)", "The formal ます/です form used in everyday polite conversation. Example: たべます, きれいです."),
    GlossaryEntry("Te-Form (て形)", "A verb or adjective form ending in て or で, used to connect clauses, make requests, or build other grammar patterns. Example: たべて."),
    GlossaryEntry("Potential Form (可能形 • かのうけい)", "A verb form expressing ability — 'can do'. Example: たべられる = can eat."),
    GlossaryEntry("Passive Form (受身形 • うけみけい)", "A verb form expressing that the subject receives an action. Example: たべられる = is eaten."),
    GlossaryEntry("Causative Form (使役形 • しえきけい)", "A verb form expressing that someone makes or lets someone else do something. Example: たべさせる = make/let eat."),
    GlossaryEntry("Causative-Passive Form (使役受身形)", "Combines causative and passive — the subject is made to do something by someone else. Example: たべさせられる = be made to eat."),
    GlossaryEntry("Volitional Form (意志形 • いしけい)", "Expresses intention or invitation — 'let's' or 'I will'. Example: たべよう = let's eat / I'll eat."),
    GlossaryEntry("Ba-Form (ば形)", "Conditional form meaning 'if'. Example: たべれば = if (you/I) eat."),
    GlossaryEntry("JLPT Levels (N5–N1)", "The Japanese Language Proficiency Test has 5 levels. N5 is beginner; N1 is the highest. Content in this app is tagged by level."),
    GlossaryEntry("Hiragana (平仮名)", "The basic Japanese syllabary, 46 characters. Used for native Japanese words, grammar particles, and verb endings."),
    GlossaryEntry("Katakana (片仮名)", "A second syllabary matching hiragana sounds. Used mainly for foreign loanwords and emphasis. Example: テレビ = TV."),
    GlossaryEntry("Kanji (漢字)", "Chinese-origin characters adopted into Japanese. Each has one or more readings (on-yomi / kun-yomi) and meanings."),
    GlossaryEntry("On-yomi (音読み)", "The Chinese-origin reading of a kanji, typically used in compound words. Example: 日 → ニチ."),
    GlossaryEntry("Kun-yomi (訓読み)", "The native Japanese reading of a kanji, typically used for standalone words. Example: 日 → ひ."),
    GlossaryEntry("Romaji (ローマ字)", "The romanisation of Japanese sounds using the Latin alphabet. Used in this app as a pronunciation guide."),
    GlossaryEntry("Furigana (振り仮名)", "Small hiragana printed above kanji to show their reading — helpful for learners."),
    GlossaryEntry("SOV Word Order", "Japanese sentences follow Subject–Object–Verb order. Example: わたしは りんごを たべます = I apple eat = I eat an apple."),
)

@Composable
fun GettingStartedScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = "Getting Started", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Key Terms & Concepts",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Before diving in, here is a quick reference for terms you will encounter throughout the app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            glossary.forEachIndexed { index, entry ->
                GlossaryCard(entry)
                if (index < glossary.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GlossaryCard(entry: GlossaryEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = entry.term,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.definition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
