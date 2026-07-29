package com.bhatt.tonerewriter.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * "Bubblegum & Sky" — a fixed blue+pink brand, expressed as full Material 3 tonal palettes for
 * both light and dark. Pink drives [primary] (buttons, selection, slider); blue rides on
 * [tertiary] (secondary actions, "try another tone") so the two colours read as different jobs
 * rather than one flat accent. Dynamic colour stays off on purpose: the identity is the point,
 * and it keeps the result-panel diff contrast predictable.
 *
 * Seeds: pink #EC4899, blue #3B82F6.
 */

// ---- Dark ----
val PinkPrimaryDark = Color(0xFFFF8FC0)
val OnPinkPrimaryDark = Color(0xFF5A0E33)
val PinkContainerDark = Color(0xFF8A2957)
val OnPinkContainerDark = Color(0xFFFFD9E7)

val PinkSecondaryDark = Color(0xFFE5A9C4)
val OnPinkSecondaryDark = Color(0xFF45182E)
val PinkSecondaryContainerDark = Color(0xFF5C3247)
val OnPinkSecondaryContainerDark = Color(0xFFFFD9E7)

val BlueTertiaryDark = Color(0xFF93C5FD)
val OnBlueTertiaryDark = Color(0xFF0A2C5A)
val BlueTertiaryContainerDark = Color(0xFF1E3A73)
val OnBlueTertiaryContainerDark = Color(0xFFD6E6FF)

val BackgroundDark = Color(0xFF141017)
val OnBackgroundDark = Color(0xFFECE3EC)
val SurfaceDark = Color(0xFF141017)
val OnSurfaceDark = Color(0xFFECE3EC)
val SurfaceVariantDark = Color(0xFF262029)
val OnSurfaceVariantDark = Color(0xFFB4A9B4)
val OutlineDark = Color(0xFF4A4250)
val OutlineVariantDark = Color(0xFF322B38)

val SurfaceContainerLowestDark = Color(0xFF0E0B12)
val SurfaceContainerLowDark = Color(0xFF1C1720)
val SurfaceContainerDark = Color(0xFF201B25)
val SurfaceContainerHighDark = Color(0xFF2A2430)
val SurfaceContainerHighestDark = Color(0xFF352E3B)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// ---- Light ----
val PinkPrimaryLight = Color(0xFFD01A6B)
val OnPinkPrimaryLight = Color(0xFFFFFFFF)
val PinkContainerLight = Color(0xFFFFD9E7)
val OnPinkContainerLight = Color(0xFF3F0021)

val PinkSecondaryLight = Color(0xFF8C4A67)
val OnPinkSecondaryLight = Color(0xFFFFFFFF)
val PinkSecondaryContainerLight = Color(0xFFFFD9E7)
val OnPinkSecondaryContainerLight = Color(0xFF37041F)

val BlueTertiaryLight = Color(0xFF1D5BD6)
val OnBlueTertiaryLight = Color(0xFFFFFFFF)
val BlueTertiaryContainerLight = Color(0xFFD8E5FF)
val OnBlueTertiaryContainerLight = Color(0xFF001A43)

val BackgroundLight = Color(0xFFFDF6FA)
val OnBackgroundLight = Color(0xFF201A20)
val SurfaceLight = Color(0xFFFDF6FA)
val OnSurfaceLight = Color(0xFF201A20)
val SurfaceVariantLight = Color(0xFFF1E4EC)
val OnSurfaceVariantLight = Color(0xFF6E5D68)
val OutlineLight = Color(0xFF857381)
val OutlineVariantLight = Color(0xFFD8C7D0)

val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFFCEFF5)
val SurfaceContainerLight = Color(0xFFF7E9F0)
val SurfaceContainerHighLight = Color(0xFFF1E3EB)
val SurfaceContainerHighestLight = Color(0xFFEBDDE5)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

// ---- Brand constants (same in both themes) ----
// The result panel deliberately inverts to a light pink surface so the rewrite reads as the
// payload, and the diff highlight is blue for genuine contrast against it. Because the panel is
// theme-independent, its controls carry fixed colours instead of the flipping scheme roles.
val ResultSurface = Color(0xFFFCE7F0)
val ResultOnSurface = Color(0xFF2A1420)
val ResultHighlight = Color(0xFFCFE0FA)
val ResultButton = Color(0xFFC21E63)
val OnResultButton = Color(0xFFFFFFFF)
val ResultTonalContainer = Color(0xFFF4CFDF)
val OnResultTonalContainer = Color(0xFF6B1B3D)
