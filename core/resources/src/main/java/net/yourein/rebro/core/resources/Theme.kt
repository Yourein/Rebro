package net.yourein.rebro.core.resources

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Rebro のブランドカラーに基づいた [androidx.compose.material3.ColorScheme]。
 *
 * Figma のデザインがダークテーマ前提のため、固定のダークスキームとして定義している。
 */
private val RebroColorScheme = darkColorScheme(
    primary = RebroColor.Brand,
    onPrimary = RebroColor.Background,
    secondary = RebroColor.Highlight,
    onSecondary = RebroColor.Background,
    tertiary = RebroColor.Highlight,
    onTertiary = RebroColor.Background,
    background = RebroColor.Background,
    onBackground = RebroColor.TextPrimary,
    surface = RebroColor.Background,
    onSurface = RebroColor.TextPrimary,
    onSurfaceVariant = RebroColor.TextSecondary,
)

@Composable
fun RebroTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RebroColorScheme,
        typography = RebroTypography,
        content = content
    )
}
