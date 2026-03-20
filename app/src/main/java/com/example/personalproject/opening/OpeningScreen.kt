package com.example.personalproject.opening

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalproject.LocalAppContainer
import com.example.personalproject.data.model.AppTheme
import com.example.personalproject.data.model.StudyDirection

@Composable
fun OpeningScreen(onStart: () -> Unit) {
    val container = LocalAppContainer.current
    val settings by container.settingsRepository.settings.collectAsState()
    var showOptions by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Options button — top right
        IconButton(
            onClick = { showOptions = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        ) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Centre content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // App logo / title
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "言",
                    fontSize = 64.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "言葉",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Kotoba",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Learn Japanese",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = "Start",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    if (showOptions) {
        OptionsDialog(
            settings = settings,
            onDismiss = { showOptions = false },
            onThemeChange = { container.settingsRepository.updateTheme(it) },
            onVolumeChange = { container.settingsRepository.updateVolume(it) },
            onRomajiChange = { container.settingsRepository.updateShowRomaji(it) },
            onFuriganaChange = { container.settingsRepository.updateShowFurigana(it) },
            onDirectionChange = { container.settingsRepository.updateStudyDirection(it) },
        )
    }
}

@Composable
private fun OptionsDialog(
    settings: com.example.personalproject.data.model.AppSettings,
    onDismiss: () -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onRomajiChange: (Boolean) -> Unit,
    onFuriganaChange: (Boolean) -> Unit,
    onDirectionChange: (StudyDirection) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        title = { Text("Options") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Volume
                SettingsSectionLabel("Volume")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Master",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(72.dp),
                    )
                    Slider(
                        value = settings.masterVolume,
                        onValueChange = onVolumeChange,
                        modifier = Modifier.weight(1f),
                    )
                }

                HorizontalDivider()

                // Display
                SettingsSectionLabel("Display")
                SettingsToggleRow("Show Romaji", settings.showRomaji, onRomajiChange)
                SettingsToggleRow("Show Furigana", settings.showFurigana, onFuriganaChange)

                HorizontalDivider()

                // Study direction
                SettingsSectionLabel("Study Direction")
                Column(modifier = Modifier.selectableGroup()) {
                    StudyDirection.entries.forEach { direction ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = settings.studyDirection == direction,
                                    onClick = { onDirectionChange(direction) },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 4.dp),
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

                HorizontalDivider()

                // Theme
                SettingsSectionLabel("Theme")
                ThemeGrid(
                    selected = settings.theme,
                    onSelect = onThemeChange,
                )
            }
        },
    )
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ThemeGrid(selected: AppTheme, onSelect: (AppTheme) -> Unit) {
    val themes = AppTheme.entries
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        themes.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { theme ->
                    val isSelected = theme == selected
                    Surface(
                        onClick = { onSelect(theme) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = if (isSelected) 4.dp else 0.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = theme.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }
                    }
                }
                // Pad if odd number
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
