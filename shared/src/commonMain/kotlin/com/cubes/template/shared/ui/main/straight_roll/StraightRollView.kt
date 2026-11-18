package com.cubes.template.shared.ui.main.straight_roll

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.cubes.template.shared.domain.straight.StraightRoll
import com.cubes.template.shared.domain.straight.StraightPhase
import com.cubes.template.shared.domain.straight.StraightState
import com.cubes.template.shared.domain.straight.StraightTurn
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.game_straight_roll
import com.cubes.template.shared.resources.game_done
import com.cubes.template.shared.resources.straight_done
import com.cubes.template.shared.resources.straight_hint
import com.cubes.template.shared.resources.straight_out_of_points
import com.cubes.template.shared.resources.straight_points
import com.cubes.template.shared.resources.straight_reroll_cost
import com.cubes.template.shared.resources.straight_turn_hint
import com.cubes.template.shared.resources.game_final_standings
import com.cubes.template.shared.resources.game_home
import com.cubes.template.shared.resources.game_new_match
import com.cubes.template.shared.resources.game_next_round
import com.cubes.template.shared.resources.game_round
import com.cubes.template.shared.resources.game_round_over
import com.cubes.template.shared.resources.game_round_points
import com.cubes.template.shared.resources.game_score_value
import com.cubes.template.shared.resources.game_turn_of
import com.cubes.template.shared.resources.game_waiting
import com.cubes.template.shared.resources.roll
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorBlack
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorPrimaryLight
import com.cubes.template.shared.ui.common.ColorSuccess
import com.cubes.template.shared.ui.common.ColorSurface
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.ColorTransparent
import com.cubes.template.shared.ui.common.ColorWarning
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.PreviewHelper
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.SemiBoldFont
import com.cubes.template.shared.ui.common.views.AnimatedDie
import com.cubes.template.shared.ui.common.views.DiceTable
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.common.views.DieFace
import com.cubes.template.shared.ui.common.views.DieStyle
import com.cubes.template.shared.ui.common.views.GameRules
import com.cubes.template.shared.ui.common.views.RankingRow
import com.cubes.template.shared.ui.common.views.RankingSection
import com.cubes.template.shared.ui.common.views.Toolbar
import com.cubes.template.shared.ui.common.views.buttons.Button
import org.jetbrains.compose.resources.stringResource

private val CARD_DIE_SIZE = 28.dp
private val TABLE_DIE_SIZE = 54.dp
private val DICE_GAP = 12.dp
private val GRIDS_GAP = 40.dp
private const val GRID_ROWS = 2
private const val SLOT_EMPTY_ALPHA = 0.25f
private const val DICE_PER_ROW = 3

@Composable
fun StraightRollView(viewModel: StraightRollViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    StraightRollScreen(viewData = viewData)
}

@Composable
fun StraightRollScreen(viewData: StraightRollUi.ViewData) {
    val state = viewData.state
    val listener = viewData.listener
    val blur by animateDpAsState(if (viewData.dialogOpen) 10.dp else 0.dp)

    Box(modifier = Modifier.fillMaxSize().background(ColorBackground)) {
        Column(modifier = Modifier.fillMaxSize().blur(blur)) {
            Toolbar(
                onBackClick = listener::onHomeClick,
                rules = GameRules.STRAIGHT_ROLL,
                title = stringResource(Res.string.game_straight_roll),
                subtitle = stringResource(Res.string.game_round, state.round, state.totalRounds),
            )

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Scoreboard(viewData)
                    if (state.phase == StraightPhase.Playing) TurnPanel(state) else ResultPanel(state)
                }

                ActionButtons(viewData)
            }
        }

        AnimatedVisibility(visible = viewData.dialogOpen, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ColorBlack.copy(alpha = 0.45f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            )
        }

        AnimatedVisibility(
            visible = viewData.dialogOpen,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
        ) {
            TableDialogContent(viewData)
        }
    }
}

@Composable
private fun TurnPanel(state: StraightState) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(Res.string.game_turn_of, state.players[state.currentPlayer]),
            fontFamily = ExtraBoldFont(),
            fontSize = 28.sp,
            color = ColorTextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.straight_turn_hint),
            fontFamily = RegularFont(),
            fontSize = 15.sp,
            color = ColorTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ResultPanel(state: StraightState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorSurface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.game_round_over, state.round, state.totalRounds),
            fontFamily = RegularFont(),
            fontSize = 14.sp,
            color = ColorTextSecondary,
        )
        if (state.phase == StraightPhase.MatchOver) {
            RankingSection(title = stringResource(Res.string.game_final_standings)) {
                state.players.indices.sortedByDescending { state.scores[it] }.forEach { index ->
                    RankingRow(
                        place = 1 + state.scores.count { it > state.scores[index] },
                        name = state.players[index],
                        detail = null,
                        value = stringResource(Res.string.game_score_value, state.scores[index]),
                    )
                }
            }
        }
    }
}

@Composable
private fun Scoreboard(viewData: StraightRollUi.ViewData) {
    val state = viewData.state
    val winners = if (state.phase == StraightPhase.MatchOver) state.leaders else emptyList()
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.players.forEachIndexed { index, name ->
            val isWinner = index in winners
            val isCurrent = state.phase == StraightPhase.Playing && index == state.currentPlayer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isWinner) ColorSuccess.copy(alpha = 0.15f) else ColorSurface)
                    .border(
                        width = 2.dp,
                        color = when {
                            isWinner -> ColorSuccess
                            isCurrent -> ColorPrimary
                            else -> ColorTransparent
                        },
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        fontFamily = SemiBoldFont(),
                        fontSize = 16.sp,
                        color = ColorTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    state.roundScores[index]?.let { points ->
                        Text(
                            text = stringResource(Res.string.game_round_points, points),
                            fontFamily = BoldFont(),
                            fontSize = 14.sp,
                            color = if (points > 0) ColorSuccess else ColorTextSecondary,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = stringResource(Res.string.game_score_value, state.scores[index]),
                        fontFamily = BoldFont(),
                        fontSize = 16.sp,
                        color = ColorPrimary,
                    )
                }
                Spacer(Modifier.height(8.dp))
                val turn = state.roundTurns[index]
                if (turn != null) {
                    FinishedTurnRow(turn, viewData.dieStyleOf(index))
                } else {
                    Text(
                        text = stringResource(Res.string.game_waiting),
                        fontFamily = RegularFont(),
                        fontSize = 14.sp,
                        color = ColorTextSecondary,
                        modifier = Modifier.height(CARD_DIE_SIZE),
                    )
                }
            }
        }
    }
}

@Composable
private fun FinishedTurnRow(turn: StraightTurn, style: DieStyle) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        (1..StraightRoll.DICE_COUNT).forEach { value ->
            val filled = value in turn.keptValues
            DieFace(
                value = value,
                size = CARD_DIE_SIZE,
                style = style,
                highlighted = turn.isStraight,
                modifier = Modifier.alpha(if (filled) 1f else SLOT_EMPTY_ALPHA),
            )
        }
    }
}

@Composable
private fun TableDialogContent(viewData: StraightRollUi.ViewData) {
    val listener = viewData.listener
    val turn = viewData.tableTurn
    val style = viewData.dieStyleOf(viewData.tablePlayer)
    val settled = turn.hasRolled && !viewData.rolling
    val enabled = viewData.dialogOpen && settled
    val over = settled && turn.isOver

    val move = remember { Animatable(0f) }
    LaunchedEffect(viewData.moving) {
        move.snapTo(0f)
        if (viewData.moving.isNotEmpty()) {
            move.animateTo(1f, tween(StraightRollUi.MOVE_MILLIS, easing = FastOutSlowInEasing))
        }
    }

    DiceTable(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Text(
            text = viewData.state.players[viewData.tablePlayer],
            fontFamily = BoldFont(),
            fontSize = 22.sp,
            color = ColorWhite,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.straight_points, turn.points),
            fontFamily = ExtraBoldFont(),
            fontSize = 22.sp,
            color = ColorWarning,
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DiceGrid { cell ->
                val value = cell + 1
                val filled = value in viewData.collected
                DieFace(
                    value = value,
                    size = TABLE_DIE_SIZE,
                    style = style,
                    highlighted = filled && over && turn.isStraight,
                    modifier = Modifier.alpha(if (filled) 1f else SLOT_EMPTY_ALPHA),
                )
            }
            Spacer(Modifier.height(GRIDS_GAP))
            DiceGrid { index ->
                if (index in viewData.onTable) {
                    val moving = index in viewData.moving
                    val slot = (turn.dice.getOrNull(index) ?: 1) - 1
                    AnimatedDie(
                        value = turn.dice.getOrNull(index),
                        rollKey = viewData.rollTicks[index],
                        size = TABLE_DIE_SIZE,
                        style = style,
                        startDelayMillis = index * DieAnimation.STAGGER_MILLIS,
                        animateOnAppear = viewData.rolling && !moving,
                        modifier = Modifier
                            .zIndex(if (moving) 1f else 0f)
                            .graphicsLayer {
                                if (moving) {
                                    val pitch = (TABLE_DIE_SIZE + DICE_GAP).toPx()
                                    val gridsDistance = (TABLE_DIE_SIZE * GRID_ROWS + DICE_GAP * (GRID_ROWS - 1) + GRIDS_GAP).toPx()
                                    val columns = slot % DICE_PER_ROW - index % DICE_PER_ROW
                                    val rows = slot / DICE_PER_ROW - index / DICE_PER_ROW
                                    translationX = columns * pitch * move.value
                                    translationY = (rows * pitch - gridsDistance) * move.value
                                }
                            },
                    )
                } else {
                    Spacer(Modifier.size(TABLE_DIE_SIZE))
                }
            }
        }

        Text(
            text = when {
                !settled -> ""
                turn.isStraight -> stringResource(Res.string.straight_done)
                turn.isOver -> stringResource(Res.string.straight_out_of_points)
                else -> stringResource(Res.string.straight_hint)
            },
            fontFamily = if (over) ExtraBoldFont() else RegularFont(),
            fontSize = if (over) 24.sp else 14.sp,
            color = if (over) ColorWarning else ColorWhite.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier.height(34.dp),
        )

        if (over) {
            Button(
                text = stringResource(Res.string.game_done),
                onClick = listener::onDoneClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ColorWhite,
                textColor = ColorPrimary,
            )
        } else {
            Button(
                text = stringResource(Res.string.straight_reroll_cost, StraightRoll.REROLL_COST),
                onClick = listener::onRollClick,
                enabled = enabled && turn.canRoll,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DiceGrid(cell: @Composable (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(DICE_GAP)) {
        (0 until StraightRoll.DICE_COUNT).chunked(DICE_PER_ROW).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(DICE_GAP)) {
                row.forEach { index -> key(index) { cell(index) } }
            }
        }
    }
}

@Composable
private fun ActionButtons(viewData: StraightRollUi.ViewData) {
    val state = viewData.state
    val listener = viewData.listener
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when (state.phase) {
            StraightPhase.Playing -> Button(
                text = stringResource(Res.string.roll),
                onClick = listener::onRollClick,
                enabled = !viewData.dialogOpen,
                modifier = Modifier.fillMaxWidth(),
            )

            StraightPhase.RoundOver -> Button(
                text = stringResource(Res.string.game_next_round),
                onClick = listener::onNextRoundClick,
                modifier = Modifier.fillMaxWidth(),
            )

            StraightPhase.MatchOver -> {
                Button(
                    text = stringResource(Res.string.game_new_match),
                    onClick = listener::onNewMatchClick,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    text = stringResource(Res.string.game_home),
                    onClick = listener::onHomeClick,
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = ColorPrimaryLight,
                    textColor = ColorPrimary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StraightRollScreenTurnPreview() {
    AppTheme { StraightRollScreen(viewData = PreviewHelper.straightRollTurn()) }
}

@Preview(showBackground = true)
@Composable
private fun StraightRollScreenDialogPreview() {
    AppTheme { StraightRollScreen(viewData = PreviewHelper.straightRollDialog()) }
}

@Preview(showBackground = true)
@Composable
private fun StraightRollScreenMatchOverPreview() {
    AppTheme { StraightRollScreen(viewData = PreviewHelper.straightRollMatchOver()) }
}
