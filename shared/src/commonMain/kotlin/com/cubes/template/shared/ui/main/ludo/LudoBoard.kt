package com.cubes.template.shared.ui.main.ludo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import com.cubes.template.shared.ui.common.sound.SoundEffect
import com.cubes.template.shared.ui.common.sound.rememberSoundEffect
import kotlinx.coroutines.coroutineScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cubes.template.shared.domain.ludo.Ludo
import com.cubes.template.shared.domain.ludo.LudoSeat
import com.cubes.template.shared.domain.ludo.LudoMove
import com.cubes.template.shared.domain.ludo.LudoState
import com.cubes.template.shared.ui.common.ColorBlack
import com.cubes.template.shared.ui.common.ColorGray
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.views.LudoPawn
import com.cubes.template.shared.ui.common.views.PawnColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val CAPTURE_SOUND_PATH = "files/ludo_capture.wav"
private const val GOAL_SOUND_PATH = "files/ludo_goal.wav"
private const val FINISH_SOUND_PATH = "files/ludo_finish.wav"

private const val DARK_COLOR_LUMINANCE = 0.06f
private const val DARK_COLOR_LIGHTENING = 0.4f

private val ColorBoard = Color(0xFFFFF4DA)
private val ColorBoardEdge = Color(0xFFE2C98F)
private val ColorCellLine = Color(0xFF5B4A2E)

object LudoGeometry {
    const val GRID = 11
    const val STEP_MILLIS = 170
    const val IMPACT_MILLIS = 140
    const val FLY_MILLIS = 620
    const val LAND_MILLIS = 260
    const val BURST_MILLIS = 560
    const val CAPTURE_MILLIS = IMPACT_MILLIS + FLY_MILLIS + LAND_MILLIS

    val track: List<Cell> = buildList {
        (0..4).forEach { add(Cell(it, 4)) }
        (3 downTo 0).forEach { add(Cell(4, it)) }
        add(Cell(5, 0))
        (0..4).forEach { add(Cell(6, it)) }
        (7..10).forEach { add(Cell(it, 4)) }
        add(Cell(10, 5))
        (10 downTo 6).forEach { add(Cell(it, 6)) }
        (7..10).forEach { add(Cell(6, it)) }
        add(Cell(5, 10))
        (10 downTo 6).forEach { add(Cell(4, it)) }
        (3 downTo 0).forEach { add(Cell(it, 6)) }
        add(Cell(0, 5))
    }

    fun goal(seat: LudoSeat): List<Cell> = when (seat) {
        LudoSeat.TOP_LEFT -> (1..4).map { Cell(it, 5) }
        LudoSeat.TOP_RIGHT -> (1..4).map { Cell(5, it) }
        LudoSeat.BOTTOM_RIGHT -> (9 downTo 6).map { Cell(it, 5) }
        LudoSeat.BOTTOM_LEFT -> (9 downTo 6).map { Cell(5, it) }
    }

    fun yard(seat: LudoSeat): List<Cell> {
        val (col, row) = when (seat) {
            LudoSeat.TOP_LEFT -> 0 to 0
            LudoSeat.TOP_RIGHT -> 9 to 0
            LudoSeat.BOTTOM_RIGHT -> 9 to 9
            LudoSeat.BOTTOM_LEFT -> 0 to 9
        }
        return listOf(Cell(col, row), Cell(col + 1, row), Cell(col, row + 1), Cell(col + 1, row + 1))
    }

    fun cellOf(seat: LudoSeat, pawn: Int, progress: Int): Cell = when {
        progress == Ludo.HOME -> yard(seat)[pawn]
        progress >= Ludo.TRACK_LENGTH -> goal(seat)[progress - Ludo.TRACK_LENGTH]
        else -> track[Ludo.cellOf(seat, progress)!!]
    }

    fun path(seat: LudoSeat, pawn: Int, from: Int, to: Int): List<Cell> = when {
        to == Ludo.HOME || from == Ludo.HOME || to < from -> listOf(cellOf(seat, pawn, to))
        else -> (from + 1..to).map { cellOf(seat, pawn, it) }
    }

    fun steps(move: LudoMove): Int =
        if (move.from == Ludo.HOME) 1 else move.to - move.from

    data class Cell(val col: Int, val row: Int) {
        val center: Offset get() = Offset(col + 0.5f, row + 0.5f)
    }
}

@Composable
fun LudoBoard(
    state: LudoState,
    pawnColors: List<PawnColor>,
    movable: Set<Int>,
    onPawnClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val playerColors = state.players.indices.map { pawnColors.getOrElse(it) { PawnColor.entries[it] }.color }
    val seatColors = state.players.mapIndexed { index, player -> player.seat to playerColors[index] }.toMap()
    val inPreview = LocalInspectionMode.current
    val captureSound = if (inPreview) null else rememberSoundEffect(CAPTURE_SOUND_PATH, voices = 2, volume = 0.9f)
    val goalSound = if (inPreview) null else rememberSoundEffect(GOAL_SOUND_PATH, voices = 2, volume = 0.8f)
    val finishSound = if (inPreview) null else rememberSoundEffect(FINISH_SOUND_PATH, voices = 1, volume = 0.9f)
    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        val cell = maxWidth / LudoGeometry.GRID
        Canvas(modifier = Modifier.fillMaxSize()) { drawBoard(seatColors) }

        val move = state.lastMove
        val captureDelay = move?.let { LudoGeometry.steps(it) * LudoGeometry.STEP_MILLIS.toLong() } ?: 0L
        if (move != null && move.from < Ludo.TRACK_LENGTH && move.to >= Ludo.TRACK_LENGTH) {
            val playerFinished = state.placeOf(move.player) != null
            LaunchedEffect(move) {
                delay(captureDelay)
                (if (playerFinished) finishSound else goalSound)?.play()
            }
        }
        if (move != null && move.captured.isNotEmpty()) {
            key(move) {
                CaptureBurst(
                    at = LudoGeometry.cellOf(state.players[move.player].seat, move.pawn, move.to).center,
                    colors = move.captured.map { (owner, _) -> playerColors[owner] },
                    cell = cell,
                    delayMillis = captureDelay,
                    sound = captureSound,
                )
            }
        }
        state.players.forEachIndexed { player, ludoPlayer ->
            state.pawns[player].forEachIndexed { pawn, progress ->
                key(player, pawn) {
                    BoardPawn(
                        seat = ludoPlayer.seat,
                        color = playerColors[player],
                        pawn = pawn,
                        progress = progress,
                        cell = cell,
                        movable = player == state.currentPlayer && pawn in movable,
                        captureDelayMillis = captureDelay,
                        onClick = { onPawnClick(pawn) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardPawn(
    seat: LudoSeat,
    color: Color,
    pawn: Int,
    progress: Int,
    cell: Dp,
    movable: Boolean,
    captureDelayMillis: Long,
    onClick: () -> Unit,
) {
    val position = remember { Animatable(LudoGeometry.cellOf(seat, pawn, progress).center, Offset.VectorConverter) }
    val hop = remember { Animatable(0f) }
    val squash = remember { Animatable(1f) }
    val spin = remember { Animatable(0f) }
    var shownProgress by remember { mutableIntStateOf(progress) }
    var moving by remember { mutableIntStateOf(0) }

    LaunchedEffect(progress) {
        if (progress == shownProgress) return@LaunchedEffect
        val from = shownProgress
        shownProgress = progress
        moving++
        if (progress == Ludo.HOME && from != Ludo.HOME) {
            delay(captureDelayMillis)
            knockedHome(LudoGeometry.cellOf(seat, pawn, progress).center, position, hop, squash, spin)
        } else {
            LudoGeometry.path(seat, pawn, from, progress).forEach { target ->
                launch {
                    hop.snapTo(0f)
                    hop.animateTo(0f, keyframes {
                        durationMillis = LudoGeometry.STEP_MILLIS
                        1f at LudoGeometry.STEP_MILLIS / 2
                    })
                }
                position.animateTo(target.center, tween(LudoGeometry.STEP_MILLIS, easing = LinearEasing))
            }
        }
        moving--
    }

    val density = LocalDensity.current
    val cellPx = with(density) { cell.toPx() }
    val pawnHeight = cell * 1.05f
    Box(
        modifier = Modifier
            .zIndex(if (moving > 0) 100f else position.value.y)
            .offset {
                IntOffset(
                    x = ((position.value.x - 0.5f) * cellPx).roundToInt(),
                    y = ((position.value.y - 0.5f) * cellPx).roundToInt(),
                )
            }
            .size(cell)
            .clickable(
                enabled = movable,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (movable) MovableRing(color = color)
        LudoPawn(
            color = color,
            height = pawnHeight,
            modifier = Modifier.graphicsLayer {
                transformOrigin = TransformOrigin(0.5f, 0.85f)
                translationY = -cellPx * (0.22f + 0.35f * hop.value)
                val grow = 1f + 0.12f * hop.value
                scaleX = grow * (1f + (1f - squash.value) * 0.6f)
                scaleY = grow * squash.value
                rotationZ = spin.value
            },
        )
    }
}

private suspend fun knockedHome(
    home: Offset,
    position: Animatable<Offset, AnimationVector2D>,
    hop: Animatable<Float, AnimationVector1D>,
    squash: Animatable<Float, AnimationVector1D>,
    spin: Animatable<Float, AnimationVector1D>,
) = coroutineScope {
    val direction = if (home.x < position.value.x) -1f else 1f
    val impact = tween<Float>(LudoGeometry.IMPACT_MILLIS, easing = FastOutLinearInEasing)
    launch { spin.animateTo(direction * 28f, impact) }
    squash.animateTo(0.55f, impact)

    launch { squash.animateTo(1f, spring(dampingRatio = 0.5f)) }
    launch {
        hop.snapTo(0f)
        hop.animateTo(0f, keyframes {
            durationMillis = LudoGeometry.FLY_MILLIS
            3.2f at LudoGeometry.FLY_MILLIS / 2 using LinearOutSlowInEasing
        })
    }
    launch {
        spin.animateTo(direction * 388f, tween(LudoGeometry.FLY_MILLIS, easing = LinearOutSlowInEasing))
        spin.snapTo(0f)
    }
    position.animateTo(home, tween(LudoGeometry.FLY_MILLIS, easing = FastOutSlowInEasing))

    squash.snapTo(0.7f)
    squash.animateTo(
        1f,
        spring(dampingRatio = 0.3f, stiffness = Spring.StiffnessMedium),
    )
}

@Composable
private fun CaptureBurst(at: Offset, colors: List<Color>, cell: Dp, delayMillis: Long, sound: SoundEffect?) {
    val progress = remember { Animatable(0f) }
    val haptics = LocalHapticFeedback.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis)
        visible = true
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        sound?.play()
        progress.animateTo(1f, tween(LudoGeometry.BURST_MILLIS, easing = LinearOutSlowInEasing))
        visible = false
    }
    if (!visible) return

    val span = cell * 3
    val density = LocalDensity.current
    val cellPx = with(density) { cell.toPx() }
    Canvas(
        modifier = Modifier
            .zIndex(200f)
            .offset {
                IntOffset(
                    x = ((at.x - 1.5f) * cellPx).roundToInt(),
                    y = ((at.y - 1.5f) * cellPx).roundToInt(),
                )
            }
            .size(span),
    ) {
        val t = progress.value
        val center = Offset(size.width / 2, size.height / 2)
        val fade = 1f - t
        val main = colors.firstOrNull() ?: ColorWhite

        drawCircle(color = ColorWhite.copy(alpha = 0.8f * fade * fade), radius = cellPx * (0.3f + 0.5f * t), center = center)
        drawCircle(
            color = main.copy(alpha = fade),
            radius = cellPx * (0.35f + 1.05f * t),
            center = center,
            style = Stroke(width = cellPx * 0.14f * fade + 1f),
        )
        val sparks = 10
        repeat(sparks) { index ->
            val angle = (index * 2 * PI / sparks + 0.3).toFloat()
            val distance = cellPx * (0.4f + 1.0f * t)
            val sparkCenter = Offset(center.x + cos(angle) * distance, center.y + sin(angle) * distance)
            drawCircle(
                color = (if (index % 2 == 0) main else ColorWhite).copy(alpha = fade),
                radius = cellPx * 0.1f * (1f - 0.5f * t),
                center = sparkCenter,
            )
        }
    }
}

@Composable
private fun MovableRing(color: Color) {
    val pulse by rememberInfiniteTransition().animateFloat(
        initialValue = 0.85f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
    )
    Canvas(modifier = Modifier.fillMaxSize().scale(pulse)) {
        drawCircle(color = color.copy(alpha = 0.35f), radius = size.minDimension * 0.48f)
        drawCircle(
            color = lerp(color, ColorBlack, 0.25f),
            radius = size.minDimension * 0.48f,
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

private fun DrawScope.drawBoard(seatColors: Map<LudoSeat, Color>) {
    val c = size.minDimension / LudoGeometry.GRID
    fun Offset.px() = Offset(x * c, y * c)
    fun tint(seat: LudoSeat, amount: Float) =
        seatColors[seat]?.let { color ->
            val lightening = if (color.luminance() < DARK_COLOR_LUMINANCE) amount * DARK_COLOR_LIGHTENING else amount
            lerp(color, ColorWhite, lightening)
        } ?: lerp(ColorGray, ColorWhite, 0.6f)

    drawRoundRect(color = ColorBoard, cornerRadius = CornerRadius(c * 0.6f))
    drawRoundRect(
        color = ColorBoardEdge,
        cornerRadius = CornerRadius(c * 0.6f),
        style = Stroke(width = c * 0.12f),
    )

    LudoSeat.entries.forEach { seat ->
        val corner = LudoGeometry.yard(seat).first()
        drawRoundRect(
            color = tint(seat, 0.7f),
            topLeft = Offset(corner.col * c + c * 0.08f, corner.row * c + c * 0.08f),
            size = Size(c * 1.84f, c * 1.84f),
            cornerRadius = CornerRadius(c * 0.4f),
        )
    }

    val trackPath = Path().apply {
        LudoGeometry.track.forEachIndexed { index, cell ->
            val p = cell.center.px()
            if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
        }
        close()
    }
    drawPath(trackPath, color = ColorCellLine.copy(alpha = 0.35f), style = Stroke(width = c * 0.1f))

    val radius = c * 0.36f
    val outline = Stroke(width = c * 0.06f)
    fun spot(cell: LudoGeometry.Cell, fill: Color, line: Color) {
        drawCircle(color = fill, radius = radius, center = cell.center.px())
        drawCircle(color = line, radius = radius, center = cell.center.px(), style = outline)
    }

    LudoGeometry.track.forEach { spot(it, ColorWhite, ColorCellLine.copy(alpha = 0.6f)) }
    LudoSeat.entries.forEach { seat ->
        val line = seatColors[seat]?.let { lerp(it, ColorBlack, 0.3f) } ?: ColorGray
        spot(LudoGeometry.track[Ludo.startCell(seat)], tint(seat, 0f), line)
        LudoGeometry.goal(seat).forEach { spot(it, tint(seat, 0.45f), line) }
        LudoGeometry.yard(seat).forEach { spot(it, tint(seat, 0.2f), line) }
    }

    val center = LudoGeometry.Cell(5, 5).center.px()
    val half = c * 0.5f
    val corners = listOf(
        Offset(center.x - half, center.y - half),
        Offset(center.x + half, center.y - half),
        Offset(center.x + half, center.y + half),
        Offset(center.x - half, center.y + half),
    )
    LudoSeat.entries.forEachIndexed { index, seat ->
        val from = corners[(index + 3) % 4]
        val to = corners[index]
        val triangle = Path().apply {
            moveTo(center.x, center.y)
            lineTo(from.x, from.y)
            lineTo(to.x, to.y)
            close()
        }
        drawPath(triangle, color = tint(seat, 0f))
    }
}

@Preview
@Composable
private fun LudoBoardPreview() {
    val state = Ludo.newGame(listOf("Ana", "Marko", "Iva", "Luka")).copy(
        pawns = listOf(
            listOf(0, 12, Ludo.HOME, 41),
            listOf(Ludo.HOME, 5, Ludo.HOME, Ludo.HOME),
            listOf(30, Ludo.HOME, Ludo.HOME, Ludo.HOME),
            listOf(Ludo.HOME, Ludo.HOME, 43, 7),
        ),
    )
    LudoBoard(
        state = state,
        pawnColors = listOf(PawnColor.PURPLE, PawnColor.YELLOW, PawnColor.BLACK, PawnColor.ORANGE),
        movable = setOf(0, 1),
        onPawnClick = {},
        modifier = Modifier.size(360.dp),
    )
}
