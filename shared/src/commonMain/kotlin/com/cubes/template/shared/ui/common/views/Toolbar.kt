package com.cubes.template.shared.ui.common.views

import androidx.compose.animation.Crossfade
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
import androidx.compose.runtime.LaunchedEffect
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
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.SemiBoldFont
import kotlinx.coroutines.delay

private val ToolbarHeight = 56.dp
private val LogoDieSize = 24.dp
private val SideButtonSpace = 56.dp
private const val LOGO_DICE_COUNT = 5
private const val LOGO_HOLD_MILLIS = 400L

@Composable
fun Toolbar(
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    rules: GameRules? = null,
    title: String? = null,
    subtitle: String? = null,
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
        if (title != null) {
            ToolbarTitle(
                title = title,
                subtitle = subtitle,
                modifier = Modifier.align(Alignment.Center).padding(horizontal = SideButtonSpace),
            )
        } else {
            LogoDice(modifier = Modifier.align(Alignment.Center))
        }
        if (rules != null) {
            RulesButton(
                onClick = { showRules = true },
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp),
            )
        }
    }
}

@Composable
private fun ToolbarTitle(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    var rollKey by remember { mutableStateOf(0) }
    var showDice by remember { mutableStateOf(false) }
    LaunchedEffect(rollKey) {
        if (rollKey == 0) return@LaunchedEffect
        showDice = true
        delay(DieAnimation.totalMillis(LOGO_DICE_COUNT) + LOGO_HOLD_MILLIS)
        showDice = false
    }

    Crossfade(
        targetState = showDice,
        modifier = modifier
            .height(ToolbarHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !showDice,
            ) { rollKey++ },
    ) { dice ->
        Box(modifier = Modifier.fillMaxWidth().height(ToolbarHeight), contentAlignment = Alignment.Center) {
            if (dice) {
                LogoDiceRow(rollKey = rollKey, animateOnAppear = true)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title.uppercase(),
                        fontFamily = ExtraBoldFont(),
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        color = ColorWhite,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontFamily = SemiBoldFont(),
                            fontSize = 12.sp,
                            color = ColorWhite.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoDice(modifier: Modifier = Modifier) {
    var rollKey by remember { mutableStateOf(0) }
    LogoDiceRow(
        rollKey = rollKey,
        animateOnAppear = false,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
        ) { rollKey++ },
    )
}

@Composable
private fun LogoDiceRow(rollKey: Int, animateOnAppear: Boolean, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = modifier) {
        repeat(LOGO_DICE_COUNT) { index ->
            AnimatedDie(
                value = 6,
                rollKey = rollKey,
                size = LogoDieSize,
                highlighted = true,
                startDelayMillis = index * DieAnimation.STAGGER_MILLIS,
                animateOnAppear = animateOnAppear,
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
        Toolbar(onBackClick = {}, rules = GameRules.DICE_POKER, title = "Dice Poker", subtitle = "Round 1 of 3")
        Toolbar(onBackClick = {})
    }
}
