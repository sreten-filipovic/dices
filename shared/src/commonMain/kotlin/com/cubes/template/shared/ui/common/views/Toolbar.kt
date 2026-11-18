package com.cubes.template.shared.ui.common.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorFeltDark
import com.cubes.template.shared.ui.common.ColorFeltLight
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.SemiBoldFont

private val ToolbarHeight = 56.dp
private val LogoDieSize = 24.dp

@Composable
fun Toolbar(
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    rules: GameRules? = null,
) {
    var showRules by remember { mutableStateOf(false) }
    if (rules != null && showRules) {
        RulesDialog(rules = rules, onDismiss = { showRules = false })
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(ColorFeltDark, ColorFeltLight, ColorFeltDark)))
            .statusBarsPadding()
            .height(ToolbarHeight)
    ) {
        if (onBackClick != null) {
            BackArrow(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp),
            )
        }
        LogoDice(modifier = Modifier.align(Alignment.Center))
        if (rules != null) {
            RulesButton(
                onClick = { showRules = true },
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp),
            )
        }
    }
}

@Composable
fun ScreenTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title.uppercase(),
            fontFamily = ExtraBoldFont(),
            fontSize = 26.sp,
            letterSpacing = 1.sp,
            color = ColorTextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontFamily = SemiBoldFont(),
                fontSize = 15.sp,
                color = ColorTextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LogoDice(modifier: Modifier = Modifier) {
    var rollKey by remember { mutableStateOf(0) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
        ) { rollKey++ },
    ) {
        repeat(5) { index ->
            AnimatedDie(
                value = 6,
                rollKey = rollKey,
                size = LogoDieSize,
                highlighted = true,
                startDelayMillis = index * DieAnimation.STAGGER_MILLIS,
            )
        }
    }
}

@Composable
private fun BackArrow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Back" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            val head = Path().apply {
                moveTo(w * 0.5f, h * 0.14f)
                lineTo(w * 0.1f, h * 0.5f)
                lineTo(w * 0.5f, h * 0.86f)
            }
            drawPath(head, color = ColorWhite, style = stroke)
            drawLine(
                color = ColorWhite,
                start = Offset(w * 0.1f, h * 0.5f),
                end = Offset(w * 0.92f, h * 0.5f),
                strokeWidth = stroke.width,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun RulesButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Rules" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            listOf(-1f, 1f).forEach { side ->
                fun x(fromSpine: Float) = w * (0.5f + side * fromSpine)
                val page = Path().apply {
                    moveTo(x(0f), h * 0.26f)
                    quadraticTo(x(0.22f), h * 0.12f, x(0.44f), h * 0.2f)
                    lineTo(x(0.44f), h * 0.78f)
                    quadraticTo(x(0.22f), h * 0.7f, x(0f), h * 0.86f)
                }
                drawPath(page, color = ColorWhite, style = stroke)
                drawLine(
                    color = ColorWhite,
                    start = Offset(x(0.14f), h * 0.4f),
                    end = Offset(x(0.32f), h * 0.36f),
                    strokeWidth = stroke.width * 0.7f,
                    cap = StrokeCap.Round,
                )
            }
            drawLine(
                color = ColorWhite,
                start = Offset(w * 0.5f, h * 0.26f),
                end = Offset(w * 0.5f, h * 0.86f),
                strokeWidth = stroke.width,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Preview
@Composable
private fun ToolbarPreview() {
    Column(Modifier.background(ColorBackground)) {
        Toolbar(onBackClick = {}, rules = GameRules.DICE_POKER)
        ScreenTitle(title = "Dice Poker", subtitle = "Round 1 of 3")
    }
}
