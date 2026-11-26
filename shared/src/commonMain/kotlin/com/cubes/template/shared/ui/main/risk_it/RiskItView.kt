package com.cubes.template.shared.ui.main.risk_it

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.domain.risk.RiskIt
import com.cubes.template.shared.domain.risk.RiskPhase
import com.cubes.template.shared.domain.risk.RiskState
import com.cubes.template.shared.domain.risk.RiskTurn
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.online_playing
import com.cubes.template.shared.resources.online_player_left
import com.cubes.template.shared.resources.online_waiting_for
import com.cubes.template.shared.resources.online_your_turn
import com.cubes.template.shared.resources.game_risk_it
import com.cubes.template.shared.resources.game_done
import com.cubes.template.shared.resources.game_round_points
import com.cubes.template.shared.resources.game_score_value
import com.cubes.template.shared.resources.risk_hint
import com.cubes.template.shared.resources.risk_no_rolls_left
import com.cubes.template.shared.resources.risk_reset
import com.cubes.template.shared.resources.risk_stop
import com.cubes.template.shared.resources.risk_turn_hint
import com.cubes.template.shared.resources.seven_rolls_left
import com.cubes.template.shared.resources.game_final_standings
import com.cubes.template.shared.resources.game_home
import com.cubes.template.shared.resources.game_new_match
import com.cubes.template.shared.resources.game_next_round
import com.cubes.template.shared.resources.game_round
import com.cubes.template.shared.resources.game_round_over
import com.cubes.template.shared.resources.game_turn_of
import com.cubes.template.shared.resources.game_waiting
import com.cubes.template.shared.resources.roll
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.main.online.turn.NextRoundSoon
import com.cubes.template.shared.ui.main.online.turn.OnlineTimer
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorAccent
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorBlack
import com.cubes.template.shared.ui.common.ColorPaletteCream
import com.cubes.template.shared.ui.common.ColorPaletteSand
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorFeltDark
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
import com.cubes.template.shared.ui.common.ShapeMedium
import com.cubes.template.shared.ui.common.views.AnimatedDie
import com.cubes.template.shared.ui.common.views.DiceTable
import com.cubes.template.shared.ui.common.views.DieFace
import com.cubes.template.shared.ui.common.views.DieStyle
import com.cubes.template.shared.ui.common.views.GameRules
import com.cubes.template.shared.ui.common.views.RankingRow
import com.cubes.template.shared.ui.common.views.RankingSection
import com.cubes.template.shared.ui.common.views.Toolbar
import com.cubes.template.shared.ui.common.views.buttons.Button
import org.jetbrains.compose.resources.stringResource

private val CARD_DIE_SIZE = 20.dp
private val TABLE_DIE_SIZE = 96.dp
private val HISTORY_DIE_SIZE = 22.dp
private const val RESET_DIE_ALPHA = 0.45f

@Composable
fun RiskItView(viewModel: RiskItViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    RiskItScreen(viewData = viewData)
}

@Composable
fun RiskItScreen(viewData: RiskItUi.ViewData) {
    val state = viewData.state
    val listener = viewData.listener
    val blur by animateDpAsState(if (viewData.dialogOpen) 10.dp else 0.dp)

    Box(modifier = Modifier.fillMaxSize().background(ColorBackground)) {
        Column(modifier = Modifier.fillMaxSize().blur(blur)) {
            Toolbar(
                onBackClick = listener::onHomeClick,
                rules = GameRules.RISK_IT,
                title = stringResource(Res.string.game_risk_it),
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
                    if (state.phase == RiskPhase.Playing) TurnPanel(viewData) else ResultPanel(state)
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
private fun TurnPanel(viewData: RiskItUi.ViewData) {
    val state = viewData.state
    val online = viewData.online
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = when {
                online == null -> stringResource(Res.string.game_turn_of, state.players[state.currentPlayer])
                viewData.isMyTurn -> stringResource(Res.string.online_your_turn)
                else -> stringResource(Res.string.online_waiting_for, state.players[state.currentPlayer])
            },
            fontFamily = ExtraBoldFont(),
            fontSize = 28.sp,
            color = ColorTextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.risk_turn_hint),
            fontFamily = RegularFont(),
            fontSize = 15.sp,
            color = ColorTextSecondary,
            textAlign = TextAlign.Center,
        )
        online?.secondsLeft?.let { OnlineTimer(it) }
    }
}

@Composable
private fun ResultPanel(state: RiskState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeMedium)
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
        if (state.phase == RiskPhase.MatchOver) {
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
private fun Scoreboard(viewData: RiskItUi.ViewData) {
    val state = viewData.state
    val winners = if (state.phase == RiskPhase.MatchOver) state.leaders else emptyList()
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.players.forEachIndexed { index, name ->
            val isWinner = index in winners
            val isCurrent = state.phase == RiskPhase.Playing && index == state.currentPlayer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeMedium)
                    .background(if (isWinner) ColorSuccess.copy(alpha = 0.15f) else ColorSurface)
                    .border(
                        width = 2.dp,
                        color = when {
                            isWinner -> ColorSuccess
                            isCurrent -> ColorPrimary
                            else -> ColorTransparent
                        },
                        shape = ShapeMedium,
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
                        text = stringResource(
                            if (index in state.left) Res.string.online_player_left else Res.string.game_waiting
                        ),
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
private fun FinishedTurnRow(turn: RiskTurn, style: DieStyle) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RollHistory(rolls = turn.rolls, dieSize = CARD_DIE_SIZE, spacing = 3.dp, style = style)
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(Res.string.game_round_points, turn.score),
            fontFamily = BoldFont(),
            fontSize = 16.sp,
            color = if (turn.score > 0) ColorSuccess else ColorTextSecondary,
        )
    }
}

@Composable
private fun RollHistory(rolls: List<Int>, dieSize: Dp, spacing: Dp, style: DieStyle) {
    val counted = rolls.size - rolls.takeLastWhile { it != RiskIt.RESET_VALUE }.size
    Row(
        modifier = Modifier.height(dieSize),
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        rolls.forEachIndexed { index, value ->
            DieFace(
                value = value,
                size = dieSize,
                style = style,
                markColor = ColorAccent.takeIf { value == RiskIt.RESET_VALUE },
                modifier = Modifier.alpha(if (index < counted) RESET_DIE_ALPHA else 1f),
            )
        }
    }
}

@Composable
private fun TableDialogContent(viewData: RiskItUi.ViewData) {
    val listener = viewData.listener
    val turn = viewData.tableTurn
    val shown = if (viewData.rolling) RiskTurn(turn.rolls.dropLast(1)) else turn
    val style = viewData.dieStyleOf(viewData.tablePlayer)
    val settled = turn.hasRolled && !viewData.rolling

    DiceTable(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Text(
            text = viewData.state.players[viewData.tablePlayer],
            fontFamily = BoldFont(),
            fontSize = 22.sp,
            color = ColorWhite,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.seven_rolls_left, shown.rollsLeft),
            fontFamily = RegularFont(),
            fontSize = 14.sp,
            color = ColorWhite.copy(alpha = 0.75f),
        )

        Spacer(Modifier.height(48.dp))
        AnimatedDie(
            value = turn.rolls.lastOrNull(),
            rollKey = viewData.rollTick,
            size = TABLE_DIE_SIZE,
            style = style,
            markColor = ColorAccent.takeIf { settled && turn.lostEverything },
            animateOnAppear = viewData.rolling,
        )

        Text(
            text = shown.score.toString(),
            fontFamily = ExtraBoldFont(),
            fontSize = 40.sp,
            color = ColorWarning,
        )
        RollHistory(rolls = shown.rolls, dieSize = HISTORY_DIE_SIZE, spacing = 4.dp, style = style)
        Text(
            text = when {
                !settled -> ""
                turn.lostEverything -> stringResource(Res.string.risk_reset)
                !turn.canRoll -> stringResource(Res.string.risk_no_rolls_left)
                else -> stringResource(Res.string.risk_hint)
            },
            fontFamily = RegularFont(),
            fontSize = 14.sp,
            color = ColorWhite.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
        )

        val enabled = viewData.dialogOpen && settled
        if (!viewData.controlsTable) {
            Text(
                text = stringResource(Res.string.online_playing, viewData.state.players[viewData.tablePlayer]),
                fontFamily = SemiBoldFont(),
                fontSize = 15.sp,
                color = ColorWhite.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.height(56.dp),
            )
        } else Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                text = stringResource(if (turn.canRoll) Res.string.risk_stop else Res.string.game_done),
                onClick = listener::onStopClick,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                backgroundColor = ColorWhite,
                textColor = ColorFeltDark,
            )
            Button(
                text = stringResource(Res.string.roll),
                onClick = listener::onRollClick,
                enabled = enabled && turn.canRoll,
                modifier = Modifier.weight(1f),
backgroundColor = ColorPaletteSand,
textColor = ColorFeltDark,
borderColor = ColorPaletteCream,
            )
        }
    }
}

@Composable
private fun ActionButtons(viewData: RiskItUi.ViewData) {
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
            RiskPhase.Playing -> if (viewData.online == null || viewData.isMyTurn) {
                Button(
                    text = stringResource(Res.string.roll),
                    onClick = listener::onRollClick,
                    enabled = !viewData.dialogOpen && viewData.canAct,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            RiskPhase.RoundOver -> if (viewData.online != null) {
                NextRoundSoon()
            } else {
                Button(
                    text = stringResource(Res.string.game_next_round),
                    onClick = listener::onNextRoundClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            RiskPhase.MatchOver -> if (viewData.online != null) {
                Button(
                    text = stringResource(Res.string.game_home),
                    onClick = listener::onHomeClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
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
private fun RiskItScreenTurnPreview() {
    AppTheme { RiskItScreen(viewData = PreviewHelper.riskItTurn()) }
}

@Preview(showBackground = true)
@Composable
private fun RiskItScreenDialogPreview() {
    AppTheme { RiskItScreen(viewData = PreviewHelper.riskItDialog()) }
}

@Preview(showBackground = true)
@Composable
private fun RiskItScreenMatchOverPreview() {
    AppTheme { RiskItScreen(viewData = PreviewHelper.riskItMatchOver()) }
}
