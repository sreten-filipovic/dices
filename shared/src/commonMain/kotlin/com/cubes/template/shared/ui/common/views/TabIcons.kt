package com.cubes.template.shared.ui.common.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cubes.template.shared.ui.common.ColorPrimary

/** A die outline showing five. */
@Composable
fun DiceIcon(color: Color, modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension
        val stroke = 2.dp.toPx()
        val inset = s * 0.12f
        drawRoundRect(
            color = color,
            topLeft = Offset(inset, inset),
            size = Size(s - 2 * inset, s - 2 * inset),
            cornerRadius = CornerRadius(s * 0.2f),
            style = Stroke(width = stroke),
        )
        val pip = s * 0.075f
        listOf(0.33f to 0.33f, 0.67f to 0.33f, 0.5f to 0.5f, 0.33f to 0.67f, 0.67f to 0.67f).forEach { (x, y) ->
            drawCircle(color = color, radius = pip, center = Offset(s * x, s * y))
        }
    }
}

/** A shopping cart. */
@Composable
fun CartIcon(color: Color, modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val cart = Path().apply {
            moveTo(w * 0.06f, h * 0.16f)
            lineTo(w * 0.2f, h * 0.16f)
            lineTo(w * 0.32f, h * 0.64f)
            lineTo(w * 0.8f, h * 0.64f)
            lineTo(w * 0.92f, h * 0.3f)
            lineTo(w * 0.24f, h * 0.3f)
        }
        drawPath(cart, color = color, style = stroke)
        drawCircle(color = color, radius = w * 0.07f, center = Offset(w * 0.38f, h * 0.82f))
        drawCircle(color = color, radius = w * 0.07f, center = Offset(w * 0.74f, h * 0.82f))
    }
}

/** A head-and-shoulders silhouette. */
@Composable
fun PersonIcon(color: Color, modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawCircle(color = color, radius = w * 0.19f, center = Offset(w * 0.5f, h * 0.3f))
        val shoulders = Path().apply {
            addArc(Rect(left = w * 0.14f, top = h * 0.58f, right = w * 0.86f, bottom = h * 1.22f), 180f, 180f)
            close()
        }
        drawPath(shoulders, color = color)
    }
}

@Preview
@Composable
private fun TabIconsPreview() {
    Row {
        DiceIcon(ColorPrimary, size = 48.dp)
        CartIcon(ColorPrimary, size = 48.dp)
        PersonIcon(ColorPrimary, size = 48.dp)
    }
}
