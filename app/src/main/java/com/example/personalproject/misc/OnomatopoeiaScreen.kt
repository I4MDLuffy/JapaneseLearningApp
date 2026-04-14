package app.kotori.japanese.misc

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.ui.components.KotobaTopBar
import app.kotori.japanese.ui.components.ScreenHelpDialog

private data class OnomatopoeiaEntry(
    val japanese: String,
    val romaji: String,
    val meaning: String,
    val exampleJp: String,
    val exampleEn: String,
    val type: String,      // 擬音語 or 擬態語
)

private data class OnomatopoeiaSection(
    val title: String,
    val emoji: String,
    val subtitle: String,
    val entries: List<OnomatopoeiaEntry>,
)

private val onomatopoeiaData = listOf(
    OnomatopoeiaSection(
        title = "Sounds of Nature",
        emoji = "🌿",
        subtitle = "Weather, animals, and environment",
        entries = listOf(
            OnomatopoeiaEntry("ざーざー", "zā-zā", "Sound of heavy rain", "雨がざーざー降っている", "It's raining heavily (pour pour)", "擬音語"),
            OnomatopoeiaEntry("ぽつぽつ", "potsu-potsu", "Sound of light rain drops", "ぽつぽつ雨が降り始めた", "Light raindrops began to fall", "擬音語"),
            OnomatopoeiaEntry("ゴロゴロ", "goro-goro", "Sound of thunder rumbling", "雷がゴロゴロ鳴っている", "Thunder is rumbling", "擬音語"),
            OnomatopoeiaEntry("ぴゅーぴゅー", "pyū-pyū", "Sound of strong wind", "ぴゅーぴゅー風が吹いている", "The wind is blowing hard", "擬音語"),
            OnomatopoeiaEntry("ワンワン", "wan-wan", "Dog barking", "犬がワンワン吠えた", "The dog barked 'woof woof'", "擬音語"),
            OnomatopoeiaEntry("ニャーニャー", "nyā-nyā", "Cat meowing", "猫がニャーニャー鳴いている", "The cat is meowing", "擬音語"),
        ),
    ),
    OnomatopoeiaSection(
        title = "Human Emotions & States",
        emoji = "😊",
        subtitle = "Feelings, moods, and physical sensations",
        entries = listOf(
            OnomatopoeiaEntry("わくわく", "waku-waku", "Feeling excited/thrilled", "旅行でわくわくしている", "I'm excited about the trip", "擬態語"),
            OnomatopoeiaEntry("どきどき", "doki-doki", "Heart pounding (nervousness/excitement)", "どきどきして眠れない", "My heart is pounding and I can't sleep", "擬態語"),
            OnomatopoeiaEntry("うきうき", "uki-uki", "Feeling cheerful/elated", "うきうきした気分", "I'm in a cheerful mood", "擬態語"),
            OnomatopoeiaEntry("ぼんやり", "bon'yari", "Feeling dazed/absent-minded", "ぼんやりと空を見ていた", "I was gazing vacantly at the sky", "擬態語"),
            OnomatopoeiaEntry("ふらふら", "fura-fura", "Feeling dizzy/unsteady", "疲れてふらふらしている", "I'm dizzy from exhaustion", "擬態語"),
            OnomatopoeiaEntry("にこにこ", "niko-niko", "Smiling happily", "彼女はにこにこしていた", "She was smiling happily", "擬態語"),
        ),
    ),
    OnomatopoeiaSection(
        title = "Movement & Textures",
        emoji = "🌊",
        subtitle = "How things move, look, or feel",
        entries = listOf(
            OnomatopoeiaEntry("ふわふわ", "fuwa-fuwa", "Fluffy, soft, floating", "ふわふわのパン", "Fluffy bread", "擬態語"),
            OnomatopoeiaEntry("つるつる", "tsuru-tsuru", "Smooth, slippery", "つるつるした床", "A slippery floor", "擬態語"),
            OnomatopoeiaEntry("ぐるぐる", "guru-guru", "Spinning around in circles", "頭がぐるぐるする", "My head is spinning", "擬態語"),
            OnomatopoeiaEntry("ぴかぴか", "pika-pika", "Sparkling, shiny, gleaming", "ぴかぴかの新車", "A gleaming new car", "擬態語"),
            OnomatopoeiaEntry("ごつごつ", "gotsu-gotsu", "Rugged, bumpy, rough", "ごつごつした岩", "A rugged rock", "擬態語"),
            OnomatopoeiaEntry("さらさら", "sara-sara", "Smooth, flowing, silky", "さらさらした髪", "Silky smooth hair", "擬態語"),
        ),
    ),
    OnomatopoeiaSection(
        title = "Food & Eating",
        emoji = "🍜",
        subtitle = "Sounds and textures related to food",
        entries = listOf(
            OnomatopoeiaEntry("もぐもぐ", "mogu-mogu", "Munching, chewing", "もぐもぐ食べている", "Munching away", "擬音語"),
            OnomatopoeiaEntry("ぱくぱく", "paku-paku", "Eating with gusto (big bites)", "ぱくぱく食べた", "Gobbled it up", "擬音語"),
            OnomatopoeiaEntry("ごくごく", "goku-goku", "Gulping down liquid", "水をごくごく飲んだ", "Gulped down the water", "擬音語"),
            OnomatopoeiaEntry("しゃきしゃき", "shaki-shaki", "Crispy, crunchy texture", "しゃきしゃきのレタス", "Crispy lettuce", "擬態語"),
            OnomatopoeiaEntry("とろとろ", "toro-toro", "Thick, gooey, melty", "とろとろのチーズ", "Gooey melted cheese", "擬態語"),
            OnomatopoeiaEntry("ぱりぱり", "pari-pari", "Crisp, flaky (like crackers)", "ぱりぱりのクッキー", "Crispy cookies", "擬音語"),
        ),
    ),
    OnomatopoeiaSection(
        title = "Actions & Efforts",
        emoji = "💪",
        subtitle = "How actions are done",
        entries = listOf(
            OnomatopoeiaEntry("せっせと", "sesse to", "Working diligently/busily", "せっせと働いている", "Working diligently", "擬態語"),
            OnomatopoeiaEntry("ちゃきちゃき", "chaki-chaki", "Briskly, efficiently", "ちゃきちゃき仕事をする", "Work briskly", "擬態語"),
            OnomatopoeiaEntry("よたよた", "yota-yota", "Tottering, staggering walk", "よたよた歩いている", "Walking with a stagger", "擬態語"),
            OnomatopoeiaEntry("すたすた", "suta-suta", "Walking briskly/with purpose", "すたすた歩いていった", "Walked off briskly", "擬態語"),
            OnomatopoeiaEntry("のろのろ", "noro-noro", "Moving slowly, sluggishly", "のろのろ動いている", "Moving sluggishly", "擬態語"),
            OnomatopoeiaEntry("きびきび", "kibi-kibi", "Moving with energy/alertness", "きびきび働く", "Work with energy", "擬態語"),
        ),
    ),
)

@Composable
fun OnomatopoeiaScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }
    var expandedSection by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("onomatopoeia")) {
            container.onboardingRepository.markScreenSeen("onomatopoeia")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Onomatopoeia",
            description = "Japanese has a rich system of sound-symbolic words:\n\n" +
                "• 擬音語 (Giongo) — words that mimic actual sounds (like 'bang' or 'splash')\n" +
                "• 擬態語 (Gitaigo) — words that describe states, feelings, or textures using sound-like patterns\n\n" +
                "These words appear constantly in everyday Japanese — in manga, conversation, and literature. " +
                "They add vividness and nuance that regular words can't capture.\n\n" +
                "Tap a section to expand and explore the entries.",
            onDismiss = { showHelp = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Onomatopoeia",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showHelp = true }) {
                    Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                }
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Tap a section to explore",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            )

            onomatopoeiaData.forEach { section ->
                val isExpanded = expandedSection == section.title
                OnomatopoeiaSection(
                    section = section,
                    isExpanded = isExpanded,
                    onToggle = { expandedSection = if (isExpanded) null else section.title },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun OnomatopoeiaSection(
    section: OnomatopoeiaSection,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = section.emoji, fontSize = 24.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = section.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp),
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                )
                section.entries.forEachIndexed { index, entry ->
                    OnomatopoeiaEntryRow(entry = entry)
                    if (index < section.entries.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnomatopoeiaEntryRow(entry: OnomatopoeiaEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = entry.japanese,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = entry.romaji,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            ) {
                Text(
                    text = entry.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Text(
            text = entry.meaning,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = entry.exampleJp,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        )
        Text(
            text = entry.exampleEn,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
        )
    }
}
