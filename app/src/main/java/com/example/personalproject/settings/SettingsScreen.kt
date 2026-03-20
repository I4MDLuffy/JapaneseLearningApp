package com.example.personalproject.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.data.model.AppTheme
import com.example.personalproject.data.model.StudyDirection
import com.example.personalproject.ui.components.KotobaTopBar

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val settings by container.settingsRepository.settings.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        KotobaTopBar(title = "Settings", onBack = onBack)

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
                    text = "Colour theme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                ThemeGrid(
                    selected = settings.theme,
                    onSelect = { container.settingsRepository.updateTheme(it) },
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

@Composable
private fun ThemeGrid(selected: AppTheme, onSelect: (AppTheme) -> Unit) {
    val themes = AppTheme.entries
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        themes.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { theme ->
                    val isSelected = theme == selected
                    Surface(
                        onClick = { onSelect(theme) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface,
                        tonalElevation = if (isSelected) 4.dp else 1.dp,
                    ) {
                        androidx.compose.foundation.layout.Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = theme.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}
