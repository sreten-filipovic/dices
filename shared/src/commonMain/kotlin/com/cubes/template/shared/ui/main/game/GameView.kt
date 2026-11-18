package com.cubes.template.shared.ui.main.game

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.domain.game.GameState
import com.cubes.template.shared.domain.game.GameEngine
import com.cubes.template.shared.domain.game.HandEvaluator
import com.cubes.template.shared.domain.game.Player
import com.cubes.template.shared.domain.game.RoundPhase
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.game_dice_poker
import com.cubes.template.shared.resources.game_done
import com.cubes.template.shared.resources.game_home
import com.cubes.template.shared.resources.game_keep
import com.cubes.template.shared.resources.game_match_winner
import com.cubes.template.shared.resources.game_new_match
import com.cubes.template.shared.resources.game_next_round
import com.cubes.template.shared.resources.game_phase_first
import com.cubes.template.shared.resources.game_phase_second
import com.cubes.template.shared.resources.game_reroll_selected
import com.cubes.template.shared.resources.game_round
import com.cubes.template.shared.resources.game_round_draw
import com.cubes.template.shared.resources.game_round_over
import com.cubes.template.shared.resources.game_round_ranking
import com.cubes.template.shared.resources.game_round_winner
import com.cubes.template.shared.resources.game_score_value
import com.cubes.template.shared.resources.game_select_hint
import com.cubes.template.shared.resources.game_turn_of
import com.cubes.template.shared.resources.game_waiting
import com.cubes.template.shared.resources.game_sits_out
import com.cubes.template.shared.resources.game_round_points
import com.cubes.template.shared.resources.game_final_standings
import com.cubes.template.shared.resources.game_play_tiebreak
import com.cubes.template.shared.resources.game_tiebreak_next
import com.cubes.template.shared.resources.game_tiebreak_over
import com.cubes.template.shared.resources.game_tiebreak
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
import com.cubes.template.shared.ui.common.handName
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

@Composable
fun GameView(viewModel: GameViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    GameScreen(viewData = viewData)
}

@Composable
fun GameScreen(viewData: Game.ViewData) {
    val state = viewData.state
    val listener = viewData.listener
    val dialog = viewData.rollDialog
    val blur by animateDpAsState(if (dialog != null) 10.dp else 0.dp)

    val lastDialog = remember { mutableStateOf(dialog) }
    if (dialog != null) lastDialog.value = dialog

    Box(modifier = Modifier.fillMaxSize().background(ColorBackground)) {
        Column(modifier = Modifier.fillMaxSize().blur(blur)) {
            Toolbar(onBackClick = listener::onHomeClick, rules = GameRules.DICE_POKER)

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                ScreenTitle(
                    title = stringResource(Res.string.game_dice_poker),
                    subtitle = if (state.isTiebreak) {
                        stringResource(Res.string.game_tiebreak)
                    } else {
                        stringResource(Res.string.game_round, state.round, state.totalRounds)
                    },
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    PlayersPanel(viewData)
                    if (state.phase.isPlaying) TurnPanel(state) else ResultPanel(state)
                }

                ActionButtons(state, listener)
            }
        }

        AnimatedVisibility(visible = dialog != null, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ColorBlack.copy(alpha = 0.45f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            )
        }

        AnimatedVisibility(
            visible = dialog != null,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
        ) {
            lastDialog.value?.let { RollDialogContent(viewData, it) }
        }
    }
}

private val RoundPhase.isPlaying: Boolean
    get() = this == RoundPhase.FirstRoll || this == RoundPhase.SecondRoll

private val RoundPhase.winnerIds: List<String>
    get() = when (this) {
        is RoundPhase.RoundOver -> winnerIds
        is RoundPhase.MatchOver -> roundWinnerIds
        else -> emptyList()
    }

@Composable
private fun PlayersPanel(viewData: Game.ViewData) {
    val state = viewData.state
    val dialog = viewData.rollDialog
    val currentId = state.currentPlayer.id.takeIf { state.phase.isPlaying }
    val hiddenDiceId = dialog?.takeIf { it.animating }?.playerId
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.turnOrder.forEach { player ->
            PlayerRow(
                player = player,
                state = state,
                dieStyle = viewData.dieStyleOf(player.id),
                isCurrent = player.id == currentId,
                isWinner = player.id in state.phase.winnerIds,
                hideDice = player.id == hiddenDiceId,
            )
        }
    }
}

@Composable
private fun PlayerRow(
    player: Player,
    state: GameState,
    dieStyle: DieStyle,
    isCurrent: Boolean,
    isWinner: Boolean,
    hideDice: Boolean,
) {
    val turn = state.turnOf(player.id)
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
                text = player.name,
                fontFamily = SemiBoldFont(),
                fontSize = 16.sp,
                color = ColorTextPrimary,
                modifier = Modifier.weight(1f),
            )
            if (!state.phase.isPlaying) {
                state.lastRoundPoints[player.id]?.let { points ->
                    Text(
                        text = stringResource(Res.string.game_round_points, points),
                        fontFamily = BoldFont(),
                        fontSize = 14.sp,
                        color = if (points > 0) ColorSuccess else ColorTextSecondary,
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }
            Text(
                text = stringResource(Res.string.game_score_value, state.scoreOf(player.id)),
                fontFamily = BoldFont(),
                fontSize = 16.sp,
                color = ColorPrimary,
            )
        }
        Spacer(Modifier.height(8.dp))
        if (hideDice) {
            Spacer(Modifier.height(28.dp))
        } else if (turn.hasRolled) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StaticDiceRow(dice = turn.dice, dieSize = 28.dp, spacing = 4.dp, style = dieStyle)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = handName(requireNotNull(turn.hand).rank),
                    fontFamily = SemiBoldFont(),
                    fontSize = 14.sp,
                    color = ColorTextSecondary,
                )
            }
        } else {
            Text(
                text = stringResource(
                    if (state.isParticipant(player.id)) Res.string.game_waiting else Res.string.game_sits_out
                ),
                fontFamily = RegularFont(),
                fontSize = 14.sp,
                color = ColorTextSecondary,
            )
        }
    }
}

@Composable
private fun StaticDiceRow(dice: List<Int>, dieSize: Dp, spacing: Dp, style: DieStyle) {
    val scoring = HandEvaluator.scoringIndices(dice)
    Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
        dice.forEachIndexed { index, value ->
            DieFace(value = value, size = dieSize, highlighted = index in scoring, style = style)
        }
    }
}

@Composable
private fun TurnPanel(state: GameState) {
    Text(
        text = stringResource(Res.string.game_turn_of, state.currentPlayer.name),
        fontFamily = ExtraBoldFont(),
        fontSize = 28.sp,
        color = ColorTextPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
    )
}

@Composable
private fun ResultPanel(state: GameState) {
    val phase = state.phase
    val winners = phase.winnerIds.mapNotNull { state.player(it)?.name }
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
            text = if (state.isTiebreak) {
                stringResource(Res.string.game_tiebreak_over)
            } else {
                stringResource(Res.string.game_round_over, state.round, state.totalRounds)
            },
            fontFamily = RegularFont(),
            fontSize = 14.sp,
            color = ColorTextSecondary,
        )
        if (phase is RoundPhase.MatchOver) {
            Text(
                text = stringResource(Res.string.game_match_winner, state.player(phase.winnerId)?.name.orEmpty()),
                fontFamily = ExtraBoldFont(),
                fontSize = 24.sp,
                color = ColorSuccess,
                textAlign = TextAlign.Center,
            )
        }

        if (state.turnOrder.size > 2) {
            RoundRankingTable(state)
        } else {
            Text(
                text = if (winners.size == 1) {
                    stringResource(Res.string.game_round_winner, winners.single())
                } else {
                    stringResource(Res.string.game_round_draw, winners.joinToString())
                },
                fontFamily = BoldFont(),
                fontSize = 18.sp,
                color = ColorTextPrimary,
                textAlign = TextAlign.Center,
            )
        }

        if (phase is RoundPhase.RoundOver && state.round >= state.totalRounds) {
            Text(
                text = stringResource(
                    Res.string.game_tiebreak_next,
                    state.leaderIds.mapNotNull { state.player(it)?.name }.joinToString(),
                ),
                fontFamily = BoldFont(),
                fontSize = 16.sp,
                color = ColorPrimary,
                textAlign = TextAlign.Center,
            )
        }

        if (phase is RoundPhase.MatchOver) {
            FinalStandingsTable(state)
        }
    }
}

@Composable
private fun RoundRankingTable(state: GameState) {
    RankingSection(title = stringResource(Res.string.game_round_ranking)) {
        GameEngine.roundRanking(state).forEach { ranked ->
            RankingRow(
                place = ranked.place,
                name = state.player(ranked.playerId)?.name.orEmpty(),
                detail = handName(ranked.hand.rank),
                value = stringResource(Res.string.game_round_points, state.lastRoundPoints[ranked.playerId] ?: 0),
            )
        }
    }
}

@Composable
private fun FinalStandingsTable(state: GameState) {
    val standings = state.players.sortedByDescending { state.scoreOf(it.id) }
    RankingSection(title = stringResource(Res.string.game_final_standings)) {
        standings.forEach { player ->
            val score = state.scoreOf(player.id)
            RankingRow(
                place = 1 + state.players.count { state.scoreOf(it.id) > score },
                name = player.name,
                detail = null,
                value = stringResource(Res.string.game_score_value, score),
            )
        }
    }
}

@Composable
private fun ActionButtons(state: GameState, listener: GameViewListener) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when (state.phase) {
            RoundPhase.FirstRoll -> Button(
                text = stringResource(Res.string.roll),
                onClick = listener::onRollClick,
                modifier = Modifier.fillMaxWidth(),
            )

            RoundPhase.SecondRoll -> Unit

            is RoundPhase.RoundOver -> Button(
                text = stringResource(
                    if (state.round >= state.totalRounds) Res.string.game_play_tiebreak else Res.string.game_next_round
                ),
                onClick = listener::onNextRoundClick,
                modifier = Modifier.fillMaxWidth(),
            )

            is RoundPhase.MatchOver -> {
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

@Composable
private fun RollDialogContent(viewData: Game.ViewData, dialog: Game.RollDialog) {
    val state = viewData.state
    val listener = viewData.listener
    val player = state.player(dialog.playerId) ?: return
    val turn = state.turnOf(player.id)
    val ticks = viewData.rollTicksOf(player.id)
    val settled = !dialog.animating
    val canSelect = settled && dialog.stage == Game.Stage.FIRST_ROLL
    val scoring = if (settled && turn.hasRolled) HandEvaluator.scoringIndices(turn.dice) else emptySet()

    DiceTable(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Text(
            text = stringResource(
                if (dialog.stage == Game.Stage.FIRST_ROLL) Res.string.game_phase_first else Res.string.game_phase_second
            ),
            fontFamily = RegularFont(),
            fontSize = 14.sp,
            color = ColorWhite.copy(alpha = 0.75f),
        )
        Text(text = player.name, fontFamily = BoldFont(), fontSize = 22.sp, color = ColorWhite)

        Spacer(Modifier.height(40.dp))
        key(player.id) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(HandEvaluator.DICE_COUNT) { index ->
                    AnimatedDie(
                        value = turn.dice.getOrNull(index),
                        rollKey = ticks[index],
                        size = 50.dp,
                        selected = index in turn.selected,
                        highlighted = index in scoring && index !in turn.selected,
                        style = viewData.dieStyleOf(player.id),
                        startDelayMillis = index * DieAnimation.STAGGER_MILLIS,
                        animateOnAppear = dialog.animating,
                        onClick = if (canSelect) ({ listener.onDieClick(index) }) else null,
                    )
                }
            }
        }

        Text(
            text = turn.hand?.takeIf { settled }?.let { handName(it.rank) } ?: "",
            fontFamily = BoldFont(),
            fontSize = 22.sp,
            color = ColorWarning,
            modifier = Modifier.height(30.dp),
        )
        Text(
            text = if (canSelect) stringResource(Res.string.game_select_hint) else "",
            fontFamily = RegularFont(),
            fontSize = 13.sp,
            color = ColorWhite.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
        )

        Box(modifier = Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
            when {
                !settled -> Unit

                dialog.stage == Game.Stage.FIRST_ROLL -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        text = stringResource(Res.string.game_keep),
                        onClick = listener::onKeepClick,
                        modifier = Modifier.weight(1f),
                        backgroundColor = ColorWhite,
                        textColor = ColorPrimary,
                    )
                    Button(
                        text = stringResource(Res.string.game_reroll_selected),
                        onClick = listener::onRerollClick,
                        enabled = turn.selected.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                    )
                }

                else -> Button(
                    text = stringResource(Res.string.game_done),
                    onClick = listener::onDoneClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenTurnPreview() {
    AppTheme { GameScreen(viewData = PreviewHelper.gameTurn()) }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenDialogPreview() {
    AppTheme { GameScreen(viewData = PreviewHelper.gameDialogFirstRoll()) }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenRoundOverPreview() {
    AppTheme { GameScreen(viewData = PreviewHelper.gameRoundOver()) }
}
