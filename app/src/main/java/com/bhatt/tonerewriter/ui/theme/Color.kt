package com.bhatt.tonerewriter.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * "Ember" — a warm neutral shell lit by a single amber accent.
 *
 * The dark scheme is the primary one: near-black browns for [surface] and its containers, amber
 * for [primary], and a muted sage on [tertiary] so the rare third role never competes with the
 * accent. The light scheme is the same amber seed inverted onto warm cream, so switching the
 * system theme changes the lighting, not the identity.
 *
 * Dynamic colour stays off on purpose. The result panel's diff highlight is [primaryContainer]
 * behind [onSurface] text, and wallpaper tinting would put that contrast out of our hands.
 *
 * Seed: amber #FFB95C.
 */

// ---- Dark ----
val AmberPrimaryDark = Color(0xFFFFB95C)
val OnAmberPrimaryDark = Color(0xFF452B00)
val AmberContainerDark = Color(0xFF5E3D00)
val OnAmberContainerDark = Color(0xFFFFDDB5)

val WarmSecondaryDark = Color(0xFFDDC3A2)
val OnWarmSecondaryDark = Color(0xFF3E2E16)
val WarmSecondaryContainerDark = Color(0xFF3A3225)
val OnWarmSecondaryContainerDark = Color(0xFFEBDFC9)

val SageTertiaryDark = Color(0xFFB7CE9C)
val OnSageTertiaryDark = Color(0xFF24350E)
val SageTertiaryContainerDark = Color(0xFF394B22)
val OnSageTertiaryContainerDark = Color(0xFFD3EAB7)

val BackgroundDark = Color(0xFF17130E)
val OnBackgroundDark = Color(0xFFEDE0D3)
val SurfaceDark = Color(0xFF17130E)
val OnSurfaceDark = Color(0xFFEDE0D3)
val SurfaceVariantDark = Color(0xFF4E4335)
val OnSurfaceVariantDark = Color(0xFFC6B7A6)
val OutlineDark = Color(0xFF8F8171)
val OutlineVariantDark = Color(0xFF3C332A)

val SurfaceContainerLowestDark = Color(0xFF100D09)
val SurfaceContainerLowDark = Color(0xFF1B1712)
val SurfaceContainerDark = Color(0xFF221D17)
val SurfaceContainerHighDark = Color(0xFF2D271F)
val SurfaceContainerHighestDark = Color(0xFF3A3229)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// ---- Light ----
val AmberPrimaryLight = Color(0xFF8A5100)
val OnAmberPrimaryLight = Color(0xFFFFFFFF)
val AmberContainerLight = Color(0xFFFFDDB5)
val OnAmberContainerLight = Color(0xFF2C1600)

val WarmSecondaryLight = Color(0xFF745943)
val OnWarmSecondaryLight = Color(0xFFFFFFFF)
val WarmSecondaryContainerLight = Color(0xFFF5E0CB)
val OnWarmSecondaryContainerLight = Color(0xFF2A1707)

val SageTertiaryLight = Color(0xFF4C6634)
val OnSageTertiaryLight = Color(0xFFFFFFFF)
val SageTertiaryContainerLight = Color(0xFFCDEDAF)
val OnSageTertiaryContainerLight = Color(0xFF0D2000)

val BackgroundLight = Color(0xFFFFF8F3)
val OnBackgroundLight = Color(0xFF211A14)
val SurfaceLight = Color(0xFFFFF8F3)
val OnSurfaceLight = Color(0xFF211A14)
val SurfaceVariantLight = Color(0xFFF2E0CF)
val OnSurfaceVariantLight = Color(0xFF52443A)
val OutlineLight = Color(0xFF85735F)
val OutlineVariantLight = Color(0xFFD7C3B0)

val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFFFF2E7)
val SurfaceContainerLight = Color(0xFFFCECE0)
val SurfaceContainerHighLight = Color(0xFFF7E6DA)
val SurfaceContainerHighestLight = Color(0xFFF1E0D4)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)
