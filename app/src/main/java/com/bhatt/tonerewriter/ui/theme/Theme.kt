package com.bhatt.tonerewriter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Fixed blue+pink brand, one light scheme and one dark. Dynamic colour is deliberately off: the
 * palette is the product's identity, and wallpaper tinting would break the diff-highlight
 * contrast the result panel relies on.
 */
private val ToneLightScheme: ColorScheme = lightColorScheme(
    primary = PinkPrimaryLight,
    onPrimary = OnPinkPrimaryLight,
    primaryContainer = PinkContainerLight,
    onPrimaryContainer = OnPinkContainerLight,
    secondary = PinkSecondaryLight,
    onSecondary = OnPinkSecondaryLight,
    secondaryContainer = PinkSecondaryContainerLight,
    onSecondaryContainer = OnPinkSecondaryContainerLight,
    tertiary = BlueTertiaryLight,
    onTertiary = OnBlueTertiaryLight,
    tertiaryContainer = BlueTertiaryContainerLight,
    onTertiaryContainer = OnBlueTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

private val ToneDarkScheme: ColorScheme = darkColorScheme(
    primary = PinkPrimaryDark,
    onPrimary = OnPinkPrimaryDark,
    primaryContainer = PinkContainerDark,
    onPrimaryContainer = OnPinkContainerDark,
    secondary = PinkSecondaryDark,
    onSecondary = OnPinkSecondaryDark,
    secondaryContainer = PinkSecondaryContainerDark,
    onSecondaryContainer = OnPinkSecondaryContainerDark,
    tertiary = BlueTertiaryDark,
    onTertiary = OnBlueTertiaryDark,
    tertiaryContainer = BlueTertiaryContainerDark,
    onTertiaryContainer = OnBlueTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

@Composable
fun ToneRewriterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) ToneDarkScheme else ToneLightScheme,
        typography = Typography,
        content = content
    )
}
