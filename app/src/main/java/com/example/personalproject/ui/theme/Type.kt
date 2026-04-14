package app.kotori.japanese.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Standard typography ───────────────────────────────────────────────────────
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    ),
)

// ── Larger typography (Accessibility: Larger Text setting) ────────────────────
val LargerAppTypography = Typography(
    displayLarge  = AppTypography.displayLarge.copy(fontSize  = 58.sp,  lineHeight = 68.sp),
    headlineLarge = AppTypography.headlineLarge.copy(fontSize = 38.sp,  lineHeight = 48.sp),
    headlineMedium= AppTypography.headlineMedium.copy(fontSize= 34.sp,  lineHeight = 44.sp),
    headlineSmall = AppTypography.headlineSmall.copy(fontSize = 26.sp,  lineHeight = 34.sp),
    titleLarge    = AppTypography.titleLarge.copy(fontSize    = 26.sp,  lineHeight = 34.sp),
    titleMedium   = AppTypography.titleMedium.copy(fontSize   = 20.sp,  lineHeight = 28.sp),
    titleSmall    = AppTypography.titleSmall.copy(fontSize    = 16.sp,  lineHeight = 24.sp),
    bodyLarge     = AppTypography.bodyLarge.copy(fontSize     = 20.sp,  lineHeight = 30.sp),
    bodyMedium    = AppTypography.bodyMedium.copy(fontSize    = 17.sp,  lineHeight = 26.sp),
    bodySmall     = AppTypography.bodySmall.copy(fontSize     = 14.sp,  lineHeight = 22.sp),
    labelLarge    = AppTypography.labelLarge.copy(fontSize    = 17.sp,  lineHeight = 24.sp),
    labelMedium   = AppTypography.labelMedium.copy(fontSize   = 14.sp,  lineHeight = 20.sp),
    labelSmall    = AppTypography.labelSmall.copy(fontSize    = 13.sp,  lineHeight = 18.sp),
)
