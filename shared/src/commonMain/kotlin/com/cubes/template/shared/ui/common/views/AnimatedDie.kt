package com.cubes.template.shared.ui.common.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cubes.template.shared.ui.common.ColorBlack
import com.cubes.template.shared.ui.common.sound.rememberDiceRollSound
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

object DieAnimation {
    const val TUMBLE_MILLIS = 850
    const val STAGGER_MILLIS = 70
    const val SETTLE_MILLIS = 300

    fun totalMillis(diceCount: Int = 5): Long =
        ((diceCount - 1) * STAGGER_MILLIS + TUMBLE_MILLIS + SETTLE_MILLIS).toLong()
}

@Composable
fun AnimatedDie(
    value: Int?,
    rollKey: Int,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    selected: Boolean = false,
    highlighted: Boolean = false,
    markColor: Color? = null,
    style: DieStyle = DieStyle.WHITE,
    startDelayMillis: Int = 0,
    animateOnAppear: Boolean = false,
    soundEnabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val initialRollKey = remember { if (animateOnAppear) Int.MIN_VALUE else rollKey }
    val targetValue by rememberUpdatedState(value)
    val sound = if (soundEnabled && !LocalInspectionMode.current) rememberDiceRollSound() else null

    var rolling by remember { mutableStateOf(false) }
    var tumbleFace by remember { mutableStateOf(1) }
    val spin = remember { Animatable(0f) }
    val lift = remember { Animatable(0f) }
    val squash = remember { Animatable(1f) }

    LaunchedEffect(rollKey) {
        if (rollKey == initialRollKey) return@LaunchedEffect
        rolling = true
        tumbleFace = Random.nextInt(1, 7)
        spin.snapTo(0f)
        lift.snapTo(0f)
        squash.snapTo(1f)
        delay(startDelayMillis.toLong())
        sound?.play()

        val direction = if (Random.nextBoolean()) 1f else -1f
        coroutineScope {
            launch {
                spin.animateTo(
                    targetValue = direction * 360f * Random.nextInt(2, 4),
                    animationSpec = tween(DieAnimation.TUMBLE_MILLIS, easing = FastOutSlowInEasing),
                )
            }
            launch {
                lift.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = DieAnimation.TUMBLE_MILLIS
                        0f at 0 using LinearOutSlowInEasing
                        1f at 290 using FastOutLinearInEasing
                        0f at 540 using LinearOutSlowInEasing
                        0.3f at 670 using FastOutLinearInEasing
                        0f at 790 using LinearOutSlowInEasing
                        0.06f at 820 using FastOutLinearInEasing
                        0f at DieAnimation.TUMBLE_MILLIS
                    },
                )
            }
            launch {
                var interval = 45L
                var elapsed = 0L
                while (elapsed < DieAnimation.TUMBLE_MILLIS - 160) {
                    tumbleFace = randomFaceOtherThan(tumbleFace)
                    delay(interval)
                    elapsed += interval
                    interval = (interval * 1.18f).toLong()
                }
            }
        }

        rolling = false
        spin.snapTo(0f)
        squash.snapTo(0.8f)
        squash.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium))
    }

    val clickModifier = if (onClick != null && !rolling) Modifier.clickable(onClick = onClick) else Modifier

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val side = this.size.minDimension
            val l = lift.value
            val width = side * (0.95f - 0.4f * l)
            val height = side * 0.22f * (1f - 0.3f * l)
            drawOval(
                color = ColorBlack.copy(alpha = 0.28f * (1f - 0.6f * l)),
                topLeft = Offset((side - width) / 2, side * 0.9f - height / 2),
                size = Size(width, height),
            )
        }
        Canvas(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    val l = lift.value
                    val s = squash.value
                    translationY = -l * this.size.height * 0.9f + (1f - s) * this.size.height * 0.5f
                    rotationZ = spin.value
                    val airScale = 1f + 0.12f * l
                    scaleX = airScale * (1f + (1f - s) * 0.6f)
                    scaleY = airScale * s
                }
                .then(clickModifier)
        ) {
            drawDieFace(
                value = if (rolling) tumbleFace else targetValue,
                selected = selected && !rolling,
                highlighted = highlighted && !rolling,
                markColor = markColor.takeIf { !rolling },
                style = style,
            )
        }
    }
}

private fun randomFaceOtherThan(face: Int): Int {
    val next = Random.nextInt(1, 6)
    return if (next >= face) next + 1 else next
}
