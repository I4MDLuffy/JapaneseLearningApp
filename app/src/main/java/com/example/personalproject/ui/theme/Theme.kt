package com.example.personalproject.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.personalproject.data.model.AppTheme

// ── Default: Japanese red + indigo ───────────────────────────────────────────

private val DefaultLightScheme = lightColorScheme(
    primary = RedPrimary,
    onPrimary = RedOnPrimary,
    primaryContainer = RedPrimaryContainer,
    onPrimaryContainer = RedOnPrimaryContainer,
    secondary = IndigoSecondary,
    onSecondary = IndigoOnSecondary,
    secondaryContainer = IndigoSecondaryContainer,
    onSecondaryContainer = IndigoOnSecondaryContainer,
    tertiary = GreenTertiary,
    onTertiary = GreenOnTertiary,
    tertiaryContainer = GreenTertiaryContainer,
    onTertiaryContainer = GreenOnTertiaryContainer,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
)

private val DefaultDarkScheme = darkColorScheme(
    primary = Color_Red200,
    onPrimary = Color_Red900,
    primaryContainer = Color_Red700,
    onPrimaryContainer = Color_Red100,
    secondary = Color_Indigo200,
    onSecondary = Color_Indigo900,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
)

// ── Sakura: soft pink / gold ─────────────────────────────────────────────────

private val SakuraScheme = lightColorScheme(
    primary = SakuraPrimary,
    onPrimary = SakuraOnPrimary,
    primaryContainer = SakuraPrimaryContainer,
    onPrimaryContainer = SakuraOnPrimaryContainer,
    secondary = SakuraSecondary,
    onSecondary = SakuraOnSecondary,
    secondaryContainer = SakuraSecondaryContainer,
    onSecondaryContainer = SakuraOnSecondaryContainer,
    background = SakuraBackground,
    surface = SakuraSurface,
)

// ── Ocean: teal / deep blue ───────────────────────────────────────────────────

private val OceanScheme = lightColorScheme(
    primary = OceanPrimary,
    onPrimary = OceanOnPrimary,
    primaryContainer = OceanPrimaryContainer,
    onPrimaryContainer = OceanOnPrimaryContainer,
    secondary = OceanSecondary,
    onSecondary = OceanOnSecondary,
    secondaryContainer = OceanSecondaryContainer,
    onSecondaryContainer = OceanOnSecondaryContainer,
    background = OceanBackground,
    surface = OceanSurface,
)

// ── Forest: dark green / amber ────────────────────────────────────────────────

private val ForestScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = ForestPrimaryContainer,
    onPrimaryContainer = ForestOnPrimaryContainer,
    secondary = ForestSecondary,
    onSecondary = ForestOnSecondary,
    secondaryContainer = ForestSecondaryContainer,
    onSecondaryContainer = ForestOnSecondaryContainer,
    background = ForestBackground,
    surface = ForestSurface,
)

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun PersonalProjectTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT, AppTheme.SAKURA, AppTheme.OCEAN, AppTheme.FOREST -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when (appTheme) {
        AppTheme.SAKURA -> SakuraScheme
        AppTheme.OCEAN -> OceanScheme
        AppTheme.FOREST -> ForestScheme
        else -> if (darkTheme) DefaultDarkScheme else DefaultLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
