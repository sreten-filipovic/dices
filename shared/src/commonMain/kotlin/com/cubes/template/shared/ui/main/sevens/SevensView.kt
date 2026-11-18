package com.cubes.template.shared.ui.main.sevens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.domain.seven.SevenPhase
import com.cubes.template.shared.domain.seven.SevenState
import com.cubes.template.shared.domain.seven.SevenTurn
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.game_final_standings
import com.cubes.template.shared.resources.game_home
import com.cubes.template.shared.resources.game_sevens
import com.cubes.template.shared.resources.game_new_match
import com.cubes.template.shared.resources.game_next_round
import com.cubes.template.shared.resources.game_round
import com.cubes.template.shared.resources.game_round_over
import com.cubes.template.shared.resources.game_round_points
import com.cubes.template.shared.resources.game_score_value
import com.cubes.template.shared.resources.game_turn_of
import com.cubes.template.shared.resources.game_waiting
import com.cubes.template.shared.resources.roll
import com.cubes.template.shared.resources.seven_bank
import com.cubes.template.shared.resources.seven_hint
import com.cubes.template.shared.resources.seven_max_rolls
import com.cubes.template.shared.resources.seven_rolls_left
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorAccent
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorBlack
import com.cubes.template.shared.ui.common.ColorOrange
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorPrimaryLight
import com.cubes.template.shared.ui.common.ColorSuccess
import com.cubes.template.shared.ui.common.ColorSurface
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.ColorTransparent
import com.cubes.template.shared.ui.common.ColorWarning
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.ColorYellow
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
import com.cubes.template.shared.ui.common.views.ScreenTitle
import com.cubes.template.shared.ui.common.views.Toolbar
import com.cubes.template.shared.ui.common.views.buttons.Button
import org.jetbrains.compose.resources.stringResource

private const val DICE_PER_ROW = 3
private val CARD_DIE_SIZE = 28.dp

private val PAIR_COLORS = listOf(ColorAccent, ColorYellow, ColorOrange)

@Composable
fun SevensView(viewModel: SevensViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    SevensScreen(viewData = viewData)
}

@Composable
fun SevensScreen(viewData: SevensUi.ViewData) {
    val state = viewData.state
    val listener = viewData.listener
    val blur by animateDpAsState(if (viewData.dialogOpen) 10.dp else 0.dp)

    Box(modifier = Modifier.fillMaxSize().background(ColorBackground)) {
        Column(modifier = Modifier.fillMaxSize().blur(blur)) {
            Toolbar(onBackClick = listener::onHomeClick, rules = GameRules.SEVENS)

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                ScreenTitle(
                    title = stringResource(Res.string.game_sevens),
                    subtitle = stringResource(Res.string.game_round, state.round, state.totalRounds),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Scoreboard(viewData)
                    if (state.phase == SevenPhase.Playing) TurnPanel(state) else ResultPanel(state)
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
private fun TurnPanel(state: SevenState) {
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
            text = stringResource(Res.string.seven_max_rolls, state.rollLimit),
            fontFamily = RegularFont(),
            fontSize = 15.sp,
            color = ColorTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ResultPanel(state: SevenState) {
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
        if (state.phase == SevenPhase.MatchOver) {
            RankingTable(
                title = stringResource(Res.string.game_final_standings),
                points = state.scores,
                players = state.players,
            ) { stringResource(Res.string.game_score_value, it) }
        }
    }
}

@Composable
private fun RankingTable(
    title: String,
    points: List<Int>,
    players: List<String>,
    value: @Composable (Int) -> String,
) {
    RankingSection(title = title) {
        players.indices.sortedByDescending { points[it] }.forEach { index ->
            RankingRow(
                place = 1 + points.count { it > points[index] },
                name = players[index],
                detail = null,
                value = value(points[index]),
            )
        }
    }
}

@Composable
private fun Scoreboard(viewData: SevensUi.ViewData) {
    val state = viewData.state
    val winners = if (state.phase == SevenPhase.MatchOver) state.leaders else emptyList()
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.players.forEachIndexed { index, name ->
            val isWinner = index in winners
            val isCurrent = state.phase == SevenPhase.Playing && index == state.currentPlayer
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
                    BankedDiceRow(turn, viewData.dieStyleOf(index))
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
private fun BankedDiceRow(turn: SevenTurn, style: DieStyle) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        turn.dice.forEachIndexed { index, value ->
            val pair = turn.pairs.indexOfFirst { index in it }
            val removed = pair >= 0
            DieFace(
                value = value,
                size = CARD_DIE_SIZE,
                style = style,
                markColor = if (removed) PAIR_COLORS[pair % PAIR_COLORS.size] else null,
                modifier = Modifier.alpha(if (removed) 0.6f else 1f),
            )
        }
    }
}

@Composable
private fun TableDialogContent(viewData: SevensUi.ViewData) {
    val state = viewData.state
    val listener = viewData.listener
    val turn = viewData.tableTurn
    DiceTable(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Text(
            text = state.players[viewData.tablePlayer],
            fontFamily = BoldFont(),
            fontSize = 22.sp,
            color = ColorWhite,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.seven_rolls_left, viewData.tableRollsLeft),
            fontFamily = RegularFont(),
            fontSize = 14.sp,
            color = ColorWhite.copy(alpha = 0.75f),
        )

        Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            viewData.rollTicks.indices.chunked(DICE_PER_ROW).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    row.forEach { index ->
                        val pair = viewData.pairsShown.indexOfFirst { index in it }
                        val removed = pair >= 0
                        AnimatedDie(
                            value = turn.dice.getOrNull(index),
                            rollKey = viewData.rollTicks[index],
                            size = 60.dp,
                            style = viewData.dieStyleOf(viewData.tablePlayer),
                            markColor = if (removed) PAIR_COLORS[pair % PAIR_COLORS.size] else null,
                            startDelayMillis = index * DieAnimation.STAGGER_MILLIS,
                            animateOnAppear = viewData.rolling,
                            modifier = Modifier.alpha(if (removed) 0.6f else 1f),
                        )
                    }
                }
            }
        }

        Text(
            text = if (turn.hasRolled && !viewData.rolling) {
                stringResource(Res.string.game_score_value, turn.score)
            } else {
                ""
            },
            fontFamily = ExtraBoldFont(),
            fontSize = 30.sp,
            color = ColorWarning,
            modifier = Modifier.height(40.dp),
        )
        Text(
            text = stringResource(Res.string.seven_hint),
            fontFamily = RegularFont(),
            fontSize = 13.sp,
            color = ColorWhite.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
        )

        val enabled = viewData.dialogOpen && !viewData.rolling
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                text = stringResource(Res.string.seven_bank),
                onClick = listener::onBankClick,
                enabled = enabled && state.canBank,
                modifier = Modifier.weight(1f),
                backgroundColor = ColorWhite,
                textColor = ColorPrimary,
            )
            Button(
                text = stringResource(Res.string.roll),
                onClick = listener::onRollClick,
                enabled = enabled && state.canRoll,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ActionButtons(viewData: SevensUi.ViewData) {
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
            SevenPhase.Playing -> Button(
                text = stringResource(Res.string.roll),
                onClick = listener::onRollClick,
                enabled = !viewData.dialogOpen,
                modifier = Modifier.fillMaxWidth(),
            )

            SevenPhase.RoundOver -> Button(
                text = stringResource(Res.string.game_next_round),
                onClick = listener::onNextRoundClick,
                modifier = Modifier.fillMaxWidth(),
            )

            SevenPhase.MatchOver -> {
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
private fun SevensScreenTurnPreview() {
    AppTheme { SevensScreen(viewData = PreviewHelper.sevensTurn()) }
}

@Preview(showBackground = true)
@Composable
private fun SevensScreenDialogPreview() {
    AppTheme { SevensScreen(viewData = PreviewHelper.sevensDialog()) }
}

@Preview(showBackground = true)
@Composable
private fun SevensScreenMatchOverPreview() {
    AppTheme { SevensScreen(viewData = PreviewHelper.sevensMatchOver()) }
}
