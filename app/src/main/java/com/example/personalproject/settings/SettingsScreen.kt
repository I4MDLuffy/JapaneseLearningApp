package com.example.personalproject.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.data.model.AppTheme
import com.example.personalproject.data.model.StudyDirection
import com.example.personalproject.ui.components.KotobaTopBar
import com.example.personalproject.ui.components.ScreenHelpDialog
import com.example.personalproject.ui.theme.SwatchAmethyst
import com.example.personalproject.ui.theme.SwatchJade
import com.example.personalproject.ui.theme.SwatchSakura
import com.example.personalproject.ui.theme.SwatchSapphire
import com.example.personalproject.ui.theme.SwatchSorbet

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val settings by container.settingsRepository.settings.collectAsState()
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!container.onboardingRepository.isScreenSeen("settings")) {
            container.onboardingRepository.markScreenSeen("settings")
            showHelp = true
        }
    }

    if (showHelp) {
        ScreenHelpDialog(
            title = "Settings",
            description = "Customise your Kotori experience.\n\n" +
                "• Theme — choose from five colour schemes: Jade, Sorbet, Sapphire, Amethyst, or Sakura\n" +
                "• Dark Mode — toggle dark mode for the selected scheme\n" +
                "• Display — toggle Romaji (Latin pronunciation) and Furigana (hiragana above kanji)\n" +
                "• Study Direction — set whether flashcards show English→Japanese or Japanese→English\n" +
                "• Learning Mode — enable Structured Learning to hide advanced forms until reached\n" +
                "• Accessibility — Larger Text increases all font sizes app-wide; High Contrast maximises text/background contrast for improved readability\n" +
                "• Audio — master volume control for pronunciation playback (coming soon)",
            onDismiss = { showHelp = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(
            title = "Settings",
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Theme ──────────────────────────────────────────────────────────

            SettingsCard(title = "Theme") {
                Text(
                    text = "Colour scheme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(12.dp))
                ThemeSwatch(
                    selected = settings.theme,
                    onSelect = { container.settingsRepository.updateTheme(it) },
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                SettingsToggleRow(
                    label = "Dark Mode",
                    description = "Switch the current scheme to dark mode",
                    checked = settings.isDarkMode,
                    onCheckedChange = { container.settingsRepository.updateDarkMode(it) },
                )
            }

            // ── Display ────────────────────────────────────────────────────────

            SettingsCard(title = "Display") {
                SettingsToggleRow(
                    label = "Show Romaji",
                    description = "Display Latin-alphabet pronunciation guides",
                    checked = settings.showRomaji,
                    onCheckedChange = { container.settingsRepository.updateShowRomaji(it) },
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsToggleRow(
                    label = "Show Furigana",
                    description = "Show hiragana above kanji characters",
                    checked = settings.showFurigana,
                    onCheckedChange = { container.settingsRepository.updateShowFurigana(it) },
                )
            }

            // ── Study Direction ────────────────────────────────────────────────

            SettingsCard(title = "Study Direction") {
                Text(
                    text = "Choose the direction of flashcard quizzes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.selectableGroup()) {
                    StudyDirection.entries.forEach { direction ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = settings.studyDirection == direction,
                                    onClick = { container.settingsRepository.updateStudyDirection(direction) },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settings.studyDirection == direction,
                                onClick = null,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(direction.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // ── Structured Learning ────────────────────────────────────────────

            SettingsCard(title = "Learning Mode") {
                SettingsToggleRow(
                    label = "Structured Learning",
                    description = "Hide advanced forms until you reach that grammar point",
                    checked = settings.structuredLearning,
                    onCheckedChange = { container.settingsRepository.updateStructuredLearning(it) },
                )
            }

            // ── Accessibility ──────────────────────────────────────────────────

            SettingsCard(title = "Accessibility") {
                SettingsToggleRow(
                    label = "Larger Text",
                    description = "Increase font size throughout the app",
                    checked = settings.largerText,
                    onCheckedChange = { container.settingsRepository.updateLargerText(it) },
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsToggleRow(
                    label = "High Contrast",
                    description = "Improve visibility with higher colour contrast",
                    checked = settings.highContrast,
                    onCheckedChange = { container.settingsRepository.updateHighContrast(it) },
                )
            }

            // ── Audio ──────────────────────────────────────────────────────────

            SettingsCard(title = "Audio") {
                Text(
                    text = "Pronunciation audio (coming soon)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Volume",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(72.dp),
                    )
                    Slider(
                        value = settings.masterVolume,
                        onValueChange = { container.settingsRepository.updateVolume(it) },
                        modifier = Modifier.weight(1f),
                        enabled = false,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ThemeSwatch(selected: AppTheme, onSelect: (AppTheme) -> Unit) {
    val swatches = listOf(
        AppTheme.JADE      to SwatchJade,
        AppTheme.SORBET    to SwatchSorbet,
        AppTheme.SAPPHIRE  to SwatchSapphire,
        AppTheme.AMETHYST  to SwatchAmethyst,
        AppTheme.SAKURA    to SwatchSakura,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        swatches.forEach { (theme, color) ->
            val isSelected = theme == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelect(theme) },
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            else Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = theme.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
