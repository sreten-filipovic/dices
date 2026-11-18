package com.cubes.template.shared.ui.common.views

import androidx.compose.ui.graphics.Color
import com.cubes.template.shared.ui.common.ColorDieBody
import com.cubes.template.shared.ui.common.ColorDieBodyShade
import com.cubes.template.shared.ui.common.ColorPip
import com.cubes.template.shared.ui.common.ColorPipLight

enum class DieStyle(
    val body: Color,
    val bodyShade: Color,
    val pip: Color,
    val pipLight: Color,
    val edge: Color,
) {
    WHITE(
        body = ColorDieBody,
        bodyShade = ColorDieBodyShade,
        pip = ColorPip,
        pipLight = ColorPipLight,
        edge = Color(0x2E1A1A2E),
    ),
    BLACK(
        body = Color(0xFF30303A),
        bodyShade = Color(0xFF0E0E12),
        pip = Color(0xFFD9DAE0),
        pipLight = Color(0xFFFFFFFF),
        edge = Color(0x40FFFFFF),
    ),
    LIGHT_GRAY(
        body = Color(0xFFD9DBE0),
        bodyShade = Color(0xFFB3B6BF),
        pip = ColorPip,
        pipLight = ColorPipLight,
        edge = Color(0x2E1A1A2E),
    ),
    DARK_GRAY(
        body = Color(0xFF858993),
        bodyShade = Color(0xFF5A5E68),
        pip = Color(0xFFD9DAE0),
        pipLight = Color(0xFFFFFFFF),
        edge = Color(0x40FFFFFF),
    ),
}
