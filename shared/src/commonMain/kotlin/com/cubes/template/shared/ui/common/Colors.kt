package com.cubes.template.shared.ui.common

import androidx.compose.ui.graphics.Color

private fun themed(light: Color, dark: Color): Color = if (ThemeState.isDark) dark else light

val ColorPaletteForest = Color(0xFF0F4532)
val ColorPaletteOlive: Color get() = themed(Color(0xFF5E7A68), Color(0xFF86A08E))
val ColorPaletteSand = Color(0xFFE2B857)
val ColorPaletteCream = Color(0xFFF6EFD9)

val ColorPrimary: Color get() = themed(ColorPaletteForest, ColorPaletteSand)
val ColorOnPrimary: Color get() = themed(Color(0xFFFFFFFF), Color(0xFF13281F))
val ColorPrimaryLight: Color get() = themed(Color(0xFFEBE2C6), Color(0xFF1F3D30))
val ColorAccent = Color(0xFFB3261E)
val ColorTextPrimary: Color get() = themed(Color(0xFF13281F), Color(0xFFF2EAD3))
val ColorTextSecondary: Color get() = themed(Color(0xFF5C6B5F), Color(0xFFA6B3A6))
val ColorWhite = Color(0xFFFFFFFF)
val ColorBlack = Color(0xFF000000)
val ColorTransparent = Color(0x00FFFFFF)
val ColorBackground: Color get() = themed(ColorPaletteCream, Color(0xFF0B1D16))
val ColorSurface: Color get() = themed(Color(0xFFFFFDF7), Color(0xFF14291F))
val ColorGray: Color get() = themed(Color(0xFF9A9A88), Color(0xFF7C8A7F))
val ColorGrayLight: Color get() = themed(Color(0xFFE8DFC8), Color(0xFF2A3C33))
val ColorDivider: Color get() = themed(Color(0xFFDDD2B6), Color(0xFF2A4537))
val ColorSuccess = Color(0xFF3F8F5A)
val ColorError = ColorAccent
val ColorWarning = ColorPaletteSand
val ColorYellow = Color(0xFFE2B857)
val ColorOrange = Color(0xFFD2843A)

val ColorFeltLight = Color(0xFF1C6B4C)
val ColorFeltDark = ColorPaletteForest
val ColorDieBody = Color(0xFFFFFDF7)
val ColorDieBodyShade = Color(0xFFEFE7D3)
val ColorDieEmpty = Color(0x33F6EFD9)
val ColorPip = Color(0xFF1A1A1A)
val ColorPipLight = Color(0xFF4A4A4A)

val ColorGold = Color(0xFFE2B857)
val ColorSilver = Color(0xFFA3A89A)
val ColorBronze = Color(0xFFA8714A)
