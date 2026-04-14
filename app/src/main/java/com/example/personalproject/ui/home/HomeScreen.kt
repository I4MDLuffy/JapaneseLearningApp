package app.kotori.japanese.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kotori.japanese.LocalAppContainer
import app.kotori.japanese.data.model.NounEntry
import app.kotori.japanese.ui.components.ScreenHelpDialog
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun HomeScreen(
    onBasicCharacters: () -> Unit,
    onBeginner: () -> Unit,
    onIntermediate: () -> Unit,
    onAdvanced: () -> Unit,
    onMaster: () -> Unit,
    onPurelyGrammar: () -> Unit,
    onQuickConversational: () -> Unit,
    onCounters: () -> Unit,
    onTermStudy: () -> Unit,
    onDialogueReading: () -> Unit,
    onReview: () -> Unit = {},
    onProgress: () -> Unit = {},
) {
    val container = LocalAppContainer.current
    var showHelp by remember { mutableStateOf(false) }
    val dueCount by container.knownRepository.getDueCount().collectAsStateWithLifecycle(initialValue = 0)

    val wordOfDay by produceState<NounEntry?>(initialValue = null) {
        val all = container.nounRepository.getAllNouns()
        if (all.isNotEmpty()) {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val dayOfYear = today.dayOfYear + today.year * 366
            value = all[dayOfYear % all.size]
        }
    }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("home")) {
            container.onboardingRepository.markScreenSeen("home")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Home",
            description = "The home screen is divided into two sections.\n\n" +
                "STRUCTURED (left) follows a step-by-step learning path: Basic Characters → Beginner → Intermediate → Advanced → Master. Work through these in order for the best results.\n\n" +
                "EXPLORE (right) lets you browse by topic at any time: Grammar, Conversational phrases, Counters, Term Study, and Dialogues.\n\n" +
                "The bottom bar gives quick access to Saved items, Games, and Settings.",
            onDismiss = { showHelp = false },
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val gridHeight = maxHeight - 96.dp  // subtract header + spacers height

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ── Header ─────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "言葉",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "  Kotori",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { showHelp = true },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Outlined.HelpOutline,
                        contentDescription = "Help",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Text(
                text = "What would you like to study?",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ── Word of the Day ────────────────────────────────────────────────
            wordOfDay?.let { word ->
                WordOfDayCard(word = word)
                Spacer(modifier = Modifier.height(6.dp))
            }

            // ── Quick access: Review + Progress ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuickAccessCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.School,
                    label = "SRS Review",
                    badge = if (dueCount > 0) dueCount else null,
                    highlighted = dueCount > 0,
                    onClick = onReview,
                )
                QuickAccessCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.BarChart,
                    label = "Progress",
                    badge = null,
                    highlighted = false,
                    onClick = onProgress,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))

            // ── Two-column grid ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Left: Structured Learning
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    SectionLabel("Structured")
                    HomeNavCard(
                        modifier = Modifier.weight(1f),
                        icon = "文",
                        title = "Basic\nCharacters",
                        subtitle = "Start here — zero knowledge",
                        onClick = onBasicCharacters,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        subtitleColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                    HomeNavCard(
                        modifier = Modifier.weight(1f),
                        icon = "🌱",
                        title = "Beginner",
                        subtitle = "N5 · foundations",
                        onClick = onBeginner,
                    )
                    HomeNavCard(
                        modifier = Modifier.weight(1f),
                        icon = "🌿",
                        title = "Intermediate",
                        subtitle = "N4–N3 · building up",
                        onClick = onIntermediate,
                    )
                    HomeNavCard(
                        modifier = Modifier.weight(1f),
                        icon = "🎋",
                        title = "Advanced",
                        subtitle = "N2–N1 · mastery",
                        onClick = onAdvanced,
                    )
                    HomeNavCard(
                        modifier = Modifier.weight(1f),
                        icon = "⛩",
                        title = "Master",
                        subtitle = "Native level",
                        onClick = onMaster,
                    )
                }

                // Right: Explore
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    SectionLabel("Explore")
                    HomeNavCard(
                        modifier = Modifier.weight(1f),
                        icon = "📝",
                        title = "Grammar",
                        subtitle = "Rules & patterns",
                        onClick = onPurelyGrammar,
                    )
                    HomeNavCard(
                        modifier = Modifier.weight(1f),
                        icon = "💬",
                        title = "Conversational",
                        subtitle = "Quick phrases",
                        onClick = onQuickConversational,
                    )
                    HomeNavCard(
                        modifier = Modifier.weight(1f),
                        icon = "🔢",
                        title = "Counters",
                        subtitle = "Japanese counting",
                        onClick = onCounters,
                    )
                    HomeNavCard(
                        modifier = Modifier.weight(1f),
                        icon = "📖",
                        title = "Term Study",
                        subtitle = "Browse by category",
                        onClick = onTermStudy,
                    )
                    HomeNavCard(
                        modifier = Modifier.weight(1f),
                        icon = "🗣",
                        title = "Dialogues",
                        subtitle = "Read conversations",
                        onClick = onDialogueReading,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickAccessCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    badge: Int?,
    highlighted: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (highlighted)
        MaterialTheme.colorScheme.tertiaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (highlighted)
        MaterialTheme.colorScheme.onTertiaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BadgedBox(
            badge = {
                if (badge != null && badge > 0) {
                    Badge { Text(badge.toString()) }
                }
            },
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = contentColor)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}

@Composable
private fun WordOfDayCard(word: NounEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Word of the day",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.65f),
                letterSpacing = 0.5.sp,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = word.kanji.ifBlank { word.hiragana },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                if (word.hiragana.isNotBlank() && word.hiragana != word.kanji) {
                    Text(
                        text = word.hiragana,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    )
                }
            }
            Text(
                text = word.meaning,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (word.jlptLevel.isNotBlank()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
            ) {
                Text(
                    text = word.jlptLevel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
    )
}

@Composable
private fun HomeNavCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = icon,
                fontSize = 22.sp,
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
