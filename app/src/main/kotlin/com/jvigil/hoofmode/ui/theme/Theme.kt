package com.jvigil.hoofmode.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Hoof Mode is a single, deliberately dark-first theme built from the brand palette.
 * There is no light variant: the four brand colors read as a dark teal-to-sage ramp
 * that only works as a dark scheme.
 */
private val HoofColorScheme = darkColorScheme(
    primary = HoofPrimary,
    onPrimary = HoofOnPrimary,
    primaryContainer = HoofSurfaceVariant,
    onPrimaryContainer = HoofOnSurface,
    secondary = HoofAccent,
    onSecondary = HoofOnPrimary,
    secondaryContainer = HoofSurfaceHigh,
    onSecondaryContainer = HoofOnSurface,
    tertiary = HoofAccent,
    onTertiary = HoofOnPrimary,
    background = HoofBackground,
    onBackground = HoofOnSurface,
    surface = HoofSurface,
    onSurface = HoofOnSurface,
    surfaceVariant = HoofSurfaceVariant,
    onSurfaceVariant = HoofOnSurfaceMuted,
    outline = HoofOutline,
    error = HoofError,
    onError = HoofOnPrimary,
    surfaceDim = HoofSurfaceDim,
    surfaceBright = HoofSurfaceBright,
    surfaceContainerLowest = HoofSurfaceContainerLowest,
    surfaceContainerLow = HoofSurfaceContainerLow,
    surfaceContainer = HoofSurfaceContainer,
    surfaceContainerHigh = HoofSurfaceContainerHigh,
    surfaceContainerHighest = HoofSurfaceContainerHighest,
    inverseSurface = HoofInverseSurface,
    inverseOnSurface = HoofInverseOnSurface,
    inversePrimary = HoofInversePrimary,
)

@Composable
fun HoofModeTheme(content: @Composable () -> Unit) {
    val colorScheme = HoofColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HoofTypography,
        content = content,
    )
}
