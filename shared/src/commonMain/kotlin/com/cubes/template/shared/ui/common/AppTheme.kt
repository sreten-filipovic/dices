package com.cubes.template.shared.ui.common

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTheme(
    content: @Composable () -> Unit,
) {
    val rippleConfiguration = RippleConfiguration(
        color = ColorDivider,
        rippleAlpha = RippleAlpha(
            draggedAlpha = 0.16f,
            focusedAlpha = 0.12f,
            hoveredAlpha = 0.08f,
            pressedAlpha = 0.24f
        )
    )

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = ColorPrimary,
            onPrimary = ColorWhite,
            secondary = ColorPaletteOlive,
            tertiary = ColorPaletteSand,
            onBackground = ColorTextPrimary,
            onSurface = ColorTextPrimary,
            outline = ColorPaletteOlive,
            background = ColorBackground,
            surface = ColorSurface,
            error = ColorError,
        ),
        shapes = AppShapes,
        typography = Typography()
    ) {
        CompositionLocalProvider(LocalRippleConfiguration provides rippleConfiguration, content)
    }
}
