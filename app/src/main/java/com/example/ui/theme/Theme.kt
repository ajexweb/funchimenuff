package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppleMonochromeScheme = lightColorScheme(
    primary = AppleBlack,
    onPrimary = ApplePureWhite,
    primaryContainer = AppleDarkGray,
    onPrimaryContainer = ApplePureWhite,
    secondary = AppleDarkGray,
    onSecondary = ApplePureWhite,
    secondaryContainer = AppleSurfaceVariant,
    onSecondaryContainer = AppleBlack,
    tertiary = AppleMidGray,
    onTertiary = ApplePureWhite,
    background = AppleBackground,
    onBackground = TextPrimary,
    surface = AppleSurface,
    onSurface = TextPrimary,
    surfaceVariant = AppleSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = AppleDarkGray,
    onError = ApplePureWhite
)

@Composable
fun BoxHunterTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppleMonochromeScheme,
        typography = Typography,
        content = content
    )
}
