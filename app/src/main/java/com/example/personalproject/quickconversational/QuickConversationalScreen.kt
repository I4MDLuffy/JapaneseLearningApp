package app.kotori.japanese.quickconversational

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.ui.components.KotobaTopBar
import app.kotori.japanese.ui.components.ScreenHelpDialog

// ── Data ──────────────────────────────────────────────────────────────────────

private data class ConversationCategory(
    val key: String,          // matches phrase.category in CSV
    val japanese: String,
    val english: String,
    val emoji: String,
)

private data class LevelSection(
    val level: String,
    val emoji: String,
    val subtitle: String,
    val categories: List<ConversationCategory>,
)

private val levelSections = listOf(
    LevelSection(
        level = "Foundations",
        emoji = "🌱",
        subtitle = "N5 — essentials for every beginner",
        categories = listOf(
            ConversationCategory("greetings",        "挨拶",   "Greetings",       "👋"),
            ConversationCategory("expressions",      "表現",   "Expressions",     "💡"),
            ConversationCategory("classroom",        "教室",   "Classroom",       "🏫"),
            ConversationCategory("daily",            "日常",   "Daily Life",      "🏠"),
        ),
    ),
    LevelSection(
        level = "Situational",
        emoji = "📍",
        subtitle = "N4 — practical phrases for common situations",
        categories = listOf(
            ConversationCategory("shopping",         "買い物", "Shopping",        "🛍"),
            ConversationCategory("restaurant",       "食事",   "Restaurant",      "🍜"),
            ConversationCategory("directions",       "道案内", "Directions",      "🗺"),
            ConversationCategory("time",             "時間",   "Time & Dates",    "🕐"),
            ConversationCategory("weather",          "天気",   "Weather",         "⛅"),
        ),
    ),
    LevelSection(
        level = "Social",
        emoji = "💬",
        subtitle = "N4–N3 — connecting and communicating",
        categories = listOf(
            ConversationCategory("self-introduction","自己紹介","Self-Introduction","🙋"),
            ConversationCategory("questions",        "質問",   "Questions",       "❓"),
            ConversationCategory("invitations",      "誘い",   "Invitations",     "🎉"),
            ConversationCategory("transport",        "交通",   "Transport",       "🚃"),
        ),
    ),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun QuickConversationalScreen(
    onBack: () -> Unit,
    onCategoryClick: (category: String) -> Unit,
) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }
    var categoryCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("conversational")) {
            container.onboardingRepository.markScreenSeen("conversational")
            showHelp = true
        }
        val all = container.phraseRepository.getAllPhrases()
        categoryCounts = all.groupBy { it.category }.mapValues { it.value.size }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Quick Conversational",
            description = "Practical phrases for real-life Japanese, organised by level.\n\n" +
                "🌱 Foundations — greetings, expressions, and daily basics you need from day one.\n\n" +
                "📍 Situational — shopping, restaurants, directions, weather: phrases for going out.\n\n" +
                "💬 Social — introduce yourself, ask questions, make invitations.\n\n" +
                "Tap any category card to browse all phrases in that topic. " +
                "Tap a phrase to see its reading and English translation.",
            onDismiss = { showHelp = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Quick Conversational  💬",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showHelp = true }) {
                    Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                }
            },
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            items(levelSections) { section ->
                LevelSectionBlock(
                    section = section,
                    categoryCounts = categoryCounts,
                    onCategoryClick = onCategoryClick,
                )
            }
        }
    }
}

// ── Level section ─────────────────────────────────────────────────────────────

@Composable
private fun LevelSectionBlock(
    section: LevelSection,
    categoryCounts: Map<String, Int>,
    onCategoryClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Section header
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(text = section.emoji, fontSize = 18.sp)
                Text(
                    text = section.level.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                )
            }
            Text(
                text = section.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            )
        }

        // Category grid — 2 columns
        section.categories.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { cat ->
                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        category = cat,
                        count = categoryCounts[cat.key] ?: 0,
                        onClick = { onCategoryClick(cat.key) },
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// ── Category card ─────────────────────────────────────────────────────────────

@Composable
private fun CategoryCard(
    modifier: Modifier = Modifier,
    category: ConversationCategory,
    count: Int,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(text = category.emoji, fontSize = 18.sp, modifier = Modifier.size(22.dp))
                Text(
                    text = category.japanese,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Column {
                Text(
                    text = category.english,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (count > 0) {
                    Text(
                        text = "$count phrases",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}
