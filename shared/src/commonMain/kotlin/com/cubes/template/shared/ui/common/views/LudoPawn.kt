package com.cubes.template.shared.ui.common.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cubes.template.shared.ui.common.ColorBlack
import com.cubes.template.shared.ui.common.ColorWhite

enum class PawnColor(val color: Color) {
    BLUE(Color(0xFF2F6FE4)),
    YELLOW(Color(0xFFFFD21F)),
    RED(Color(0xFFE53935)),
    GREEN(Color(0xFF7EDC5A)),
    PURPLE(Color(0xFF8E4FD8)),
    BLACK(Color(0xFF2E2E36)),
    ORANGE(Color(0xFFFF8A1F)),
    BROWN(Color(0xFF8B5A2B));

    companion object {
        fun defaultsFor(playerCount: Int): List<PawnColor> = when (playerCount) {
            2 -> listOf(BLUE, RED)
            3 -> listOf(BLUE, YELLOW, RED)
            else -> listOf(BLUE, YELLOW, RED, GREEN)
        }

        fun resolve(chosen: List<PawnColor?>): List<PawnColor> {
            val defaults = defaultsFor(chosen.size)
            val taken = chosen.filterNotNull().toMutableSet()
            return chosen.mapIndexed { index, color ->
                color ?: (listOfNotNull(defaults.getOrNull(index)) + defaults + entries)
                    .first { it !in taken }
                    .also { taken += it }
            }
        }
    }
}

/** A shaded board-game pawn: round base, flared body and a ball head. Width is [height] × 0.7. */
@Composable
fun LudoPawn(color: Color, modifier: Modifier = Modifier, height: Dp = 40.dp) {
    Canvas(modifier = modifier.size(width = height * 0.7f, height = height)) {
        val w = size.width
        val h = size.height
        val dark = lerp(color, ColorBlack, 0.3f)
        val light = lerp(color, ColorWhite, 0.45f)
        val shade = Brush.horizontalGradient(listOf(light, color, dark), startX = 0f, endX = w)

        // Shadow on the table.
        drawOval(
            color = ColorBlack.copy(alpha = 0.25f),
            topLeft = Offset(w * 0.04f, h * 0.88f),
            size = Size(w * 0.92f, h * 0.12f),
        )
        // Base.
        drawOval(brush = shade, topLeft = Offset(w * 0.06f, h * 0.76f), size = Size(w * 0.88f, h * 0.2f))
        // Body flaring from the neck down to the base.
        val body = Path().apply {
            moveTo(w * 0.38f, h * 0.38f)
            cubicTo(w * 0.34f, h * 0.6f, w * 0.14f, h * 0.72f, w * 0.12f, h * 0.86f)
            lineTo(w * 0.88f, h * 0.86f)
            cubicTo(w * 0.86f, h * 0.72f, w * 0.66f, h * 0.6f, w * 0.62f, h * 0.38f)
            close()
        }
        drawPath(body, brush = shade)
        // Collar.
        drawOval(brush = shade, topLeft = Offset(w * 0.26f, h * 0.34f), size = Size(w * 0.48f, h * 0.08f))
        // Head with a highlight.
        val headCenter = Offset(w * 0.5f, h * 0.2f)
        val headRadius = w * 0.24f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(light, color, dark),
                center = Offset(headCenter.x - headRadius * 0.35f, headCenter.y - headRadius * 0.35f),
                radius = headRadius * 1.6f,
            ),
            radius = headRadius,
            center = headCenter,
        )
    }
}

@Preview
@Composable
private fun LudoPawnPreview() {
    Row {
        PawnColor.entries.forEach { LudoPawn(it.color, height = 60.dp) }
    }
}
