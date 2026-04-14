package com.example.personalproject.misc

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.components.ScreenHelpDialog

// ── Conversation data ─────────────────────────────────────────────────────────

private data class ConversationGroup(
    val title: String,
    val titleJp: String,
    val level: String,         // "Beginner" | "Intermediate" | "Advanced"
    val ids: List<String>,
    val preview: String,       // first Japanese line (shown on card)
)

private val conversations = listOf(
    // ── Beginner ──────────────────────────────────────────────────────────────
    ConversationGroup(
        title = "First Meeting",
        titleJp = "はじめまして",
        level = "Beginner",
        ids = listOf("d001","d002","d003","d004","d005","d006","d007"),
        preview = "はじめまして。田中です。どうぞよろしく。",
    ),
    ConversationGroup(
        title = "Asking for Directions",
        titleJp = "道案内",
        level = "Beginner",
        ids = listOf("d008","d009","d010","d011"),
        preview = "すみません、駅はどこですか。",
    ),
    ConversationGroup(
        title = "At a Restaurant",
        titleJp = "レストランで",
        level = "Beginner",
        ids = listOf("d012","d013","d014","d015","d016","d017","d018"),
        preview = "いらっしゃいませ。何名様ですか。",
    ),
    ConversationGroup(
        title = "Shopping",
        titleJp = "買い物",
        level = "Beginner",
        ids = listOf("d019","d020","d021","d022","d023"),
        preview = "このシャツはいくらですか。",
    ),
    // ── Intermediate ──────────────────────────────────────────────────────────
    ConversationGroup(
        title = "Talking About the Weather",
        titleJp = "天気の話",
        level = "Intermediate",
        ids = listOf("d024","d025","d026","d027"),
        preview = "今日の天気はどうですか。",
    ),
    ConversationGroup(
        title = "Health & Feelings",
        titleJp = "気分",
        level = "Intermediate",
        ids = listOf("d028","d029","d030"),
        preview = "気分はどうですか。",
    ),
    ConversationGroup(
        title = "Making Plans",
        titleJp = "週末の計画",
        level = "Intermediate",
        ids = listOf("d031","d032","d033","d034","d035","d036"),
        preview = "今週末、何か予定がありますか。",
    ),
    ConversationGroup(
        title = "Japanese Study",
        titleJp = "日本語の勉強",
        level = "Intermediate",
        ids = listOf("d037","d038","d039","d040"),
        preview = "日本語の勉強はどうですか。",
    ),
    // ── Advanced ──────────────────────────────────────────────────────────────
    ConversationGroup(
        title = "On the Train",
        titleJp = "電車で",
        level = "Advanced",
        ids = listOf("d041","d042","d043","d044","d045"),
        preview = "この電車は新宿に止まりますか。",
    ),
    ConversationGroup(
        title = "Talking About Interests",
        titleJp = "好きなこと",
        level = "Advanced",
        ids = listOf("d046","d047","d048","d049"),
        preview = "これ、好きですか。",
    ),
    ConversationGroup(
        title = "Making a Reservation",
        titleJp = "予約",
        level = "Advanced",
        ids = listOf("d050","d051","d052","d053","d054","d055","d056"),
        preview = "予約をしたいのですが。",
    ),
    ConversationGroup(
        title = "Checking on Someone",
        titleJp = "心配",
        level = "Advanced",
        ids = listOf("d057","d058"),
        preview = "田中さんのことが心配です。",
    ),
    ConversationGroup(
        title = "Plans for the Day",
        titleJp = "今日の予定",
        level = "Advanced",
        ids = listOf("d059","d060"),
        preview = "今日は何をしますか。",
    ),
)

private val levelOrder = listOf("Beginner", "Intermediate", "Advanced")

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun DialogueReadingScreen(
    onBack: () -> Unit,
    onConversationClick: (title: String, ids: String) -> Unit,
) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("dialogues")) {
            container.onboardingRepository.markScreenSeen("dialogues")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Dialogues",
            description = "Read short Japanese conversations organised by level.\n\n" +
                "🌱 Beginner — introductions, directions, restaurants, shopping.\n\n" +
                "🌿 Intermediate — weather, health, plans, and study talk.\n\n" +
                "🎋 Advanced — trains, reservations, interests, and more.\n\n" +
                "Tap a conversation to open the full dialogue. " +
                "Each line alternates between two speakers. " +
                "Toggle English translation and romaji on the reading screen.",
            onDismiss = { showHelp = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Dialogues",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showHelp = true }) {
                    Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                }
            },
        )

        val grouped = conversations.groupBy { it.level }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            levelOrder.forEach { level ->
                val group = grouped[level] ?: return@forEach
                item {
                    LevelHeader(level)
                }
                items(group) { conv ->
                    ConversationCard(
                        conversation = conv,
                        onClick = {
                            onConversationClick(conv.title, conv.ids.joinToString(","))
                        },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ── Level header ──────────────────────────────────────────────────────────────

@Composable
private fun LevelHeader(level: String) {
    val (emoji, subtitle) = when (level) {
        "Beginner"     -> "🌱" to "N5 · everyday basics"
        "Intermediate" -> "🌿" to "N4–N3 · natural conversation"
        "Advanced"     -> "🎋" to "N2–N1 · fluent exchanges"
        else           -> "📖" to ""
    }
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Text(
                text = level.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
            )
        }
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            )
        }
    }
}

// ── Conversation card ─────────────────────────────────────────────────────────

@Composable
private fun ConversationCard(
    conversation: ConversationGroup,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.titleJp,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = conversation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = conversation.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${conversation.ids.size} lines",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
        }
    }
}
