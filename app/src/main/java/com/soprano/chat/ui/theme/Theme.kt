package com.soprano.chat.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SopranoDarkColorScheme = darkColorScheme(
    primary            = MatteBronze,
    onPrimary          = Ivory,
    primaryContainer   = RoseGold,
    onPrimaryContainer = Ivory,
    secondary          = SlateGray,
    onSecondary        = Ivory,
    secondaryContainer = DeepSlate,
    onSecondaryContainer = SoftGray,
    tertiary           = RoseGold,
    onTertiary         = Ivory,
    background         = Anthracite,
    onBackground       = Ivory,
    surface            = SlateGray,
    onSurface          = Ivory,
    surfaceVariant     = DeepSlate,
    onSurfaceVariant   = SoftGray,
    outline            = MutedGray,
    outlineVariant     = FrostedBorder
)

@Composable
fun SopranoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SopranoDarkColorScheme,
        typography  = SopranoTypography,
        shapes      = SopranoShapes,
        content     = content
    )
}
