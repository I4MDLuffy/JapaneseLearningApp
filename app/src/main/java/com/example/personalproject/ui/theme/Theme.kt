package app.kotori.japanese.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.kotori.japanese.data.model.AppTheme

// ── Jade ──────────────────────────────────────────────────────────────────────
private val JadeLightScheme = lightColorScheme(
    primary                = JadeGreen,
    onPrimary              = JadeOnGreen,
    primaryContainer       = JadeGreenContainer,
    onPrimaryContainer     = JadeOnGreenContainer,
    secondary              = JadeTeal,
    onSecondary            = JadeOnTeal,
    secondaryContainer     = JadeTealContainer,
    onSecondaryContainer   = JadeOnTealContainer,
    background             = JadeBackground,
    onBackground           = JadeOnBackground,
    surface                = JadeSurface,
    onSurface              = JadeOnSurface,
    surfaceVariant         = JadeSurfaceVariant,
    onSurfaceVariant       = JadeOnSurfaceVariant,
    outline                = JadeOutline,
)

private val JadeDarkScheme = darkColorScheme(
    primary                = JadeDarkPrimary,
    onPrimary              = JadeDarkOnPrimary,
    primaryContainer       = JadeDarkPrimaryContainer,
    onPrimaryContainer     = JadeDarkOnPrimaryContainer,
    secondary              = JadeDarkSecondary,
    onSecondary            = JadeDarkOnSecondary,
    secondaryContainer     = JadeDarkSecondaryContainer,
    onSecondaryContainer   = JadeDarkOnSecondaryContainer,
    background             = JadeDarkBackground,
    onBackground           = JadeDarkOnBackground,
    surface                = JadeDarkSurface,
    onSurface              = JadeDarkOnSurface,
    surfaceVariant         = JadeDarkSurfaceVariant,
    onSurfaceVariant       = JadeDarkOnSurfaceVariant,
    outline                = JadeDarkOutline,
)

// ── Sorbet ────────────────────────────────────────────────────────────────────
private val SorbetLightScheme = lightColorScheme(
    primary                = SorbetRose,
    onPrimary              = SorbetOnRose,
    primaryContainer       = SorbetRoseContainer,
    onPrimaryContainer     = SorbetOnRoseContainer,
    secondary              = SorbetSage,
    onSecondary            = SorbetOnSage,
    secondaryContainer     = SorbetSageContainer,
    onSecondaryContainer   = SorbetOnSageContainer,
    background             = SorbetBackground,
    onBackground           = SorbetOnBackground,
    surface                = SorbetSurface,
    onSurface              = SorbetOnSurface,
    surfaceVariant         = SorbetSurfaceVariant,
    onSurfaceVariant       = SorbetOnSurfaceVariant,
    outline                = SorbetOutline,
)

private val SorbetDarkScheme = darkColorScheme(
    primary                = SorbetDarkPrimary,
    onPrimary              = SorbetDarkOnPrimary,
    primaryContainer       = SorbetDarkPrimaryContainer,
    onPrimaryContainer     = SorbetDarkOnPrimaryContainer,
    secondary              = SorbetDarkSecondary,
    onSecondary            = SorbetDarkOnSecondary,
    secondaryContainer     = SorbetDarkSecondaryContainer,
    onSecondaryContainer   = SorbetDarkOnSecondaryContainer,
    background             = SorbetDarkBackground,
    onBackground           = SorbetDarkOnBackground,
    surface                = SorbetDarkSurface,
    onSurface              = SorbetDarkOnSurface,
    surfaceVariant         = SorbetDarkSurfaceVariant,
    onSurfaceVariant       = SorbetDarkOnSurfaceVariant,
    outline                = SorbetDarkOutline,
)

// ── Sapphire ──────────────────────────────────────────────────────────────────
private val SapphireLightScheme = lightColorScheme(
    primary                = SapphireBlue,
    onPrimary              = SapphireOnBlue,
    primaryContainer       = SapphireBlueContainer,
    onPrimaryContainer     = SapphireOnBlueContainer,
    secondary              = SapphireMedBlue,
    onSecondary            = SapphireOnMedBlue,
    secondaryContainer     = SapphireMedBlueContainer,
    onSecondaryContainer   = SapphireOnMedBlueContainer,
    tertiary               = SapphireTeal,
    onTertiary             = SapphireOnTeal,
    tertiaryContainer      = SapphireTealContainer,
    onTertiaryContainer    = SapphireOnTealContainer,
    background             = SapphireBackground,
    onBackground           = SapphireOnBackground,
    surface                = SapphireSurface,
    onSurface              = SapphireOnSurface,
    surfaceVariant         = SapphireSurfaceVariant,
    onSurfaceVariant       = SapphireOnSurfaceVariant,
    outline                = SapphireOutline,
)

private val SapphireDarkScheme = darkColorScheme(
    primary                = SapphireDarkPrimary,
    onPrimary              = SapphireDarkOnPrimary,
    primaryContainer       = SapphireDarkPrimaryContainer,
    onPrimaryContainer     = SapphireDarkOnPrimaryContainer,
    secondary              = SapphireDarkSecondary,
    onSecondary            = SapphireDarkOnSecondary,
    secondaryContainer     = SapphireDarkSecondaryContainer,
    onSecondaryContainer   = SapphireDarkOnSecondaryContainer,
    tertiary               = SapphireDarkTertiary,
    onTertiary             = SapphireDarkOnTertiary,
    tertiaryContainer      = SapphireDarkTertiaryContainer,
    onTertiaryContainer    = SapphireDarkOnTertiaryContainer,
    background             = SapphireDarkBackground,
    onBackground           = SapphireDarkOnBackground,
    surface                = SapphireDarkSurface,
    onSurface              = SapphireDarkOnSurface,
    surfaceVariant         = SapphireDarkSurfaceVariant,
    onSurfaceVariant       = SapphireDarkOnSurfaceVariant,
    outline                = SapphireDarkOutline,
)

// ── Amethyst ──────────────────────────────────────────────────────────────────
private val AmethystLightScheme = lightColorScheme(
    primary                = AmethystPurple,
    onPrimary              = AmethystOnPurple,
    primaryContainer       = AmethystPurpleContainer,
    onPrimaryContainer     = AmethystOnPurpleContainer,
    secondary              = AmethystRose,
    onSecondary            = AmethystOnRose,
    secondaryContainer     = AmethystRoseContainer,
    onSecondaryContainer   = AmethystOnRoseContainer,
    tertiary               = AmethystGold,
    onTertiary             = AmethystOnGold,
    tertiaryContainer      = AmethystGoldContainer,
    onTertiaryContainer    = AmethystOnGoldContainer,
    background             = AmethystBackground,
    onBackground           = AmethystOnBackground,
    surface                = AmethystSurface,
    onSurface              = AmethystOnSurface,
    surfaceVariant         = AmethystSurfaceVariant,
    onSurfaceVariant       = AmethystOnSurfaceVariant,
    outline                = AmethystOutline,
)

private val AmethystDarkScheme = darkColorScheme(
    primary                = AmethystDarkPrimary,
    onPrimary              = AmethystDarkOnPrimary,
    primaryContainer       = AmethystDarkPrimaryContainer,
    onPrimaryContainer     = AmethystDarkOnPrimaryContainer,
    secondary              = AmethystDarkSecondary,
    onSecondary            = AmethystDarkOnSecondary,
    secondaryContainer     = AmethystDarkSecondaryContainer,
    onSecondaryContainer   = AmethystDarkOnSecondaryContainer,
    tertiary               = AmethystDarkTertiary,
    onTertiary             = AmethystDarkOnTertiary,
    tertiaryContainer      = AmethystDarkTertiaryContainer,
    onTertiaryContainer    = AmethystDarkOnTertiaryContainer,
    background             = AmethystDarkBackground,
    onBackground           = AmethystDarkOnBackground,
    surface                = AmethystDarkSurface,
    onSurface              = AmethystDarkOnSurface,
    surfaceVariant         = AmethystDarkSurfaceVariant,
    onSurfaceVariant       = AmethystDarkOnSurfaceVariant,
    outline                = AmethystDarkOutline,
)

// ── Sakura ────────────────────────────────────────────────────────────────────
private val SakuraLightScheme = lightColorScheme(
    primary                = SakuraPink,
    onPrimary              = SakuraOnPink,
    primaryContainer       = SakuraPinkContainer,
    onPrimaryContainer     = SakuraOnPinkContainer,
    secondary              = SakuraMidPink,
    onSecondary            = SakuraOnMidPink,
    secondaryContainer     = SakuraMidPinkContainer,
    onSecondaryContainer   = SakuraOnMidPinkContainer,
    tertiary               = SakuraLavender,
    onTertiary             = SakuraOnLavender,
    tertiaryContainer      = SakuraLavenderContainer,
    onTertiaryContainer    = SakuraOnLavenderContainer,
    background             = SakuraBackground,
    onBackground           = SakuraOnBackground,
    surface                = SakuraSurface,
    onSurface              = SakuraOnSurface,
    surfaceVariant         = SakuraSurfaceVariant,
    onSurfaceVariant       = SakuraOnSurfaceVariant,
    outline                = SakuraOutline,
)

private val SakuraDarkScheme = darkColorScheme(
    primary                = SakuraDarkPrimary,
    onPrimary              = SakuraDarkOnPrimary,
    primaryContainer       = SakuraDarkPrimaryContainer,
    onPrimaryContainer     = SakuraDarkOnPrimaryContainer,
    secondary              = SakuraDarkSecondary,
    onSecondary            = SakuraDarkOnSecondary,
    secondaryContainer     = SakuraDarkSecondaryContainer,
    onSecondaryContainer   = SakuraDarkOnSecondaryContainer,
    tertiary               = SakuraDarkTertiary,
    onTertiary             = SakuraDarkOnTertiary,
    tertiaryContainer      = SakuraDarkTertiaryContainer,
    onTertiaryContainer    = SakuraDarkOnTertiaryContainer,
    background             = SakuraDarkBackground,
    onBackground           = SakuraDarkOnBackground,
    surface                = SakuraDarkSurface,
    onSurface              = SakuraDarkOnSurface,
    surfaceVariant         = SakuraDarkSurfaceVariant,
    onSurfaceVariant       = SakuraDarkOnSurfaceVariant,
    outline                = SakuraDarkOutline,
)

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun PersonalProjectTheme(
    appTheme: AppTheme = AppTheme.JADE,
    isDarkMode: Boolean = false,
    largerText: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val baseScheme = when (appTheme) {
        AppTheme.JADE      -> if (isDarkMode) JadeDarkScheme      else JadeLightScheme
        AppTheme.SORBET    -> if (isDarkMode) SorbetDarkScheme    else SorbetLightScheme
        AppTheme.SAPPHIRE  -> if (isDarkMode) SapphireDarkScheme  else SapphireLightScheme
        AppTheme.AMETHYST  -> if (isDarkMode) AmethystDarkScheme  else AmethystLightScheme
        AppTheme.SAKURA    -> if (isDarkMode) SakuraDarkScheme    else SakuraLightScheme
    }

    val colorScheme = if (highContrast) {
        val text    = if (isDarkMode) Color.White else Color.Black
        val subText = if (isDarkMode) Color(0xFFDDDDDD) else Color(0xFF111111)
        val bg      = if (isDarkMode) Color.Black else Color.White
        val surface = if (isDarkMode) Color(0xFF0D0D0D) else Color(0xFFFAFAFA)
        baseScheme.copy(
            onBackground       = text,
            onSurface          = text,
            onSurfaceVariant   = subText,
            background         = bg,
            surface            = surface,
        )
    } else {
        baseScheme
    }

    val typography = if (largerText) LargerAppTypography else AppTypography

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content,
    )
}
