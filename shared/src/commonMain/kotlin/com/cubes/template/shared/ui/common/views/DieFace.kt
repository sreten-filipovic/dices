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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cubes.template.shared.ui.common.ColorAccent
import com.cubes.template.shared.ui.common.ColorDivider
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorWhite

/**
 * One die drawn on a Canvas. `value == null` draws an empty (not yet rolled) die.
 * [selected] marks a die chosen for reroll, [highlighted] marks a die that is part of the combination.
 */
@Composable
fun DieFace(
    value: Int?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    selected: Boolean = false,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Canvas(modifier = modifier.size(size).then(clickModifier)) {
        drawDie(value = value, selected = selected, highlighted = highlighted)
    }
}

private fun DrawScope.drawDie(value: Int?, selected: Boolean, highlighted: Boolean) {
    val side = size.minDimension
    val corner = CornerRadius(side * 0.18f)
    val stroke = side * 0.05f
    val inset = stroke / 2

    drawRoundRect(
        color = if (value == null) ColorDivider else ColorWhite,
        topLeft = Offset(inset, inset),
        size = Size(side - stroke, side - stroke),
        cornerRadius = corner,
    )
    drawRoundRect(
        color = when {
            selected -> ColorAccent
            highlighted -> ColorPrimary
            else -> ColorTextPrimary.copy(alpha = 0.25f)
        },
        topLeft = Offset(inset, inset),
        size = Size(side - stroke, side - stroke),
        cornerRadius = corner,
        style = Stroke(width = if (selected || highlighted) stroke * 2 else stroke),
    )

    if (value == null) return
    val pipColor = if (selected) ColorAccent else ColorTextPrimary
    val radius = side * 0.09f
    pipPositions(value).forEach { (fx, fy) ->
        drawCircle(color = pipColor, radius = radius, center = Offset(side * fx, side * fy))
    }
}

private const val L = 0.27f
private const val M = 0.5f
private const val H = 0.73f

/** Pip centers as fractions of the die size. */
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
    }
}
