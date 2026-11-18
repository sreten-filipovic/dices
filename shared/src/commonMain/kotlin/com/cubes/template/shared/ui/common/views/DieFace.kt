package com.cubes.template.shared.ui.common.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cubes.template.shared.ui.common.ColorAccent
import com.cubes.template.shared.ui.common.ColorDieBody
import com.cubes.template.shared.ui.common.ColorDieEmpty
import com.cubes.template.shared.ui.common.ColorPrimary

@Composable
fun DieFace(
    value: Int?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    selected: Boolean = false,
    highlighted: Boolean = false,
    markColor: Color? = null,
    style: DieStyle = DieStyle.WHITE,
    onClick: (() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Canvas(modifier = modifier.size(size).then(clickModifier)) {
        drawDieFace(value = value, selected = selected, highlighted = highlighted, markColor = markColor, style = style)
    }
}

internal fun DrawScope.drawDieFace(
    value: Int?,
    selected: Boolean,
    highlighted: Boolean,
    markColor: Color? = null,
    style: DieStyle = DieStyle.WHITE,
) {
    val side = size.minDimension
    val corner = CornerRadius(side * 0.2f)
    val ringColor = markColor ?: when {
        selected -> ColorAccent
        highlighted -> ColorPrimary
        else -> null
    }

    if (ringColor != null) {
        val glow = side * 0.08f
        drawRoundRect(
            color = ringColor.copy(alpha = 0.35f),
            topLeft = Offset(-glow, -glow),
            size = Size(side + glow * 2, side + glow * 2),
            cornerRadius = CornerRadius(corner.x + glow),
        )
    }

    drawRoundRect(
        brush = if (value == null) SolidColor(ColorDieEmpty) else Brush.linearGradient(
            colors = listOf(style.body, style.bodyShade),
            start = Offset.Zero,
            end = Offset(side, side),
        ),
        size = Size(side, side),
        cornerRadius = corner,
    )

    val edge = side * 0.03f
    drawRoundRect(
        color = if (value == null) ColorDieBody.copy(alpha = 0.5f) else style.edge,
        topLeft = Offset(edge / 2, edge / 2),
        size = Size(side - edge, side - edge),
        cornerRadius = corner,
        style = Stroke(width = edge),
    )

    if (ringColor != null) {
        val ring = side * 0.06f
        drawRoundRect(
            color = ringColor,
            topLeft = Offset(ring / 2, ring / 2),
            size = Size(side - ring, side - ring),
            cornerRadius = corner,
            style = Stroke(width = ring),
        )
    }

    if (value == null) return

    val radius = side * 0.09f
    pipPositions(value).forEach { (fx, fy) ->
        val center = Offset(side * fx, side * fy)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(if (selected) ColorAccent else style.pipLight, style.pip),
                center = center + Offset(radius * 0.35f, radius * 0.35f),
                radius = radius * 1.3f,
            ),
            radius = radius,
            center = center,
        )
    }
}

private const val L = 0.27f
private const val M = 0.5f
private const val H = 0.73f

private fun pipPositions(value: Int): List<Pair<Float, Float>> = when (value) {
    1 -> listOf(M to M)
    2 -> listOf(L to L, H to H)
    3 -> listOf(L to L, M to M, H to H)
    4 -> listOf(L to L, H to L, L to H, H to H)
    5 -> listOf(L to L, H to L, M to M, L to H, H to H)
    6 -> listOf(L to L, H to L, L to M, H to M, L to H, H to H)
    else -> emptyList()
}

@Preview
@Composable
private fun DieFacePreview() {
    Row {
        (1..6).forEach { DieFace(value = it, selected = it == 3, highlighted = it == 5) }
        DieFace(value = null)
        DieStyle.entries.forEach { DieFace(value = 5, style = it) }
    }
}
