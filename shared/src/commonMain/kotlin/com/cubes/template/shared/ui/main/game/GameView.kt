package com.cubes.template.shared.ui.main.game

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.domain.game.GameState
import com.cubes.template.shared.domain.game.HandEvaluator
import com.cubes.template.shared.domain.game.Player
import com.cubes.template.shared.domain.game.RoundPhase
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.game_home
import com.cubes.template.shared.resources.game_keep
import com.cubes.template.shared.resources.game_match_winner
import com.cubes.template.shared.resources.game_new_match
import com.cubes.template.shared.resources.game_next_round
import com.cubes.template.shared.resources.game_pass_last_roll
import com.cubes.template.shared.resources.game_pass_title
import com.cubes.template.shared.resources.game_phase_first
import com.cubes.template.shared.resources.game_phase_second
import com.cubes.template.shared.resources.game_ready
import com.cubes.template.shared.resources.game_reroll_selected
import com.cubes.template.shared.resources.game_round
import com.cubes.template.shared.resources.game_round_draw
import com.cubes.template.shared.resources.game_round_over
import com.cubes.template.shared.resources.game_round_winner
import com.cubes.template.shared.resources.game_score_value
import com.cubes.template.shared.resources.game_select_hint
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
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.PreviewHelper
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.SemiBoldFont
import com.cubes.template.shared.ui.common.handName
import com.cubes.template.shared.ui.common.views.DieFace
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

    Box(modifier = Modifier.fillMaxSize().background(ColorBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Toolbar(
                title = stringResource(Res.string.game_round, state.round),
                onBackClick = listener::onHomeClick,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PlayersPanel(state)
                when (state.phase) {
                    RoundPhase.FirstRoll, RoundPhase.SecondRoll -> CurrentTurnPanel(state, listener)
                    is RoundPhase.RoundOver, is RoundPhase.MatchOver -> ResultPanel(state)
                }
            }

            ActionButtons(state, listener)
        }

        viewData.passDevice?.let { PassDeviceOverlay(it, onReady = listener::onReadyClick) }
    }
}

private val RoundPhase.winnerIds: List<String>
    get() = when (this) {
        is RoundPhase.RoundOver -> winnerIds
        is RoundPhase.MatchOver -> roundWinnerIds
        else -> emptyList()
    }

@Composable
private fun PlayersPanel(state: GameState) {
    val playing = state.phase == RoundPhase.FirstRoll || state.phase == RoundPhase.SecondRoll
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.turnOrder.forEach { player ->
            val isCurrent = playing && player.id == state.currentPlayer.id
            val isWinner = player.id in state.phase.winnerIds
            PlayerRow(
                player = player,
                state = state,
                isCurrent = isCurrent,
                isWinner = isWinner,
            )
        }
    }
}

@Composable
private fun PlayerRow(player: Player, state: GameState, isCurrent: Boolean, isWinner: Boolean) {
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
            Text(
                text = stringResource(Res.string.game_score_value, state.scoreOf(player.id), state.targetWins),
                fontFamily = BoldFont(),
                fontSize = 16.sp,
                color = ColorPrimary,
            )
        }
        Spacer(Modifier.height(8.dp))
        if (turn.hasRolled) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DiceRow(dice = turn.dice, dieSize = 28.dp, spacing = 4.dp)
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
                text = stringResource(Res.string.game_waiting),
                fontFamily = RegularFont(),
                fontSize = 14.sp,
                color = ColorTextSecondary,
            )
        }
    }
}

@Composable
private fun DiceRow(
    dice: List<Int?>,
    dieSize: Dp,
    spacing: Dp,
    selected: Set<Int> = emptySet(),
    onDieClick: ((Int) -> Unit)? = null,
) {
    val filled = dice.filterNotNull()
    val scoring = if (filled.size == HandEvaluator.DICE_COUNT) HandEvaluator.scoringIndices(filled) else emptySet()
    Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
        dice.forEachIndexed { index, value ->
            DieFace(
                value = value,
                size = dieSize,
                selected = index in selected,
                highlighted = index in scoring && index !in selected,
                onClick = onDieClick?.let { { it(index) } },
            )
        }
    }
}

@Composable
private fun CurrentTurnPanel(state: GameState, listener: GameViewListener) {
    val turn = state.currentTurn
    val isSecond = state.phase == RoundPhase.SecondRoll
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorSurface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(if (isSecond) Res.string.game_phase_second else Res.string.game_phase_first),
            fontFamily = RegularFont(),
            fontSize = 14.sp,
            color = ColorTextSecondary,
        )
        Text(
            text = stringResource(Res.string.game_turn_of, state.currentPlayer.name),
            fontFamily = BoldFont(),
            fontSize = 20.sp,
            color = ColorTextPrimary,
        )
        DiceRow(
            dice = List(HandEvaluator.DICE_COUNT) { turn.dice.getOrNull(it) },
            dieSize = 54.dp,
            spacing = 8.dp,
            selected = turn.selected,
            onDieClick = if (isSecond) listener::onDieClick else null,
        )
        turn.hand?.let {
            Text(text = handName(it.rank), fontFamily = BoldFont(), fontSize = 18.sp, color = ColorPrimary)
        }
        if (isSecond) {
            Text(
                text = stringResource(Res.string.game_select_hint),
                fontFamily = RegularFont(),
                fontSize = 13.sp,
                color = ColorTextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
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
            text = stringResource(Res.string.game_round_over, state.round),
            fontFamily = RegularFont(),
            fontSize = 14.sp,
            color = ColorTextSecondary,
        )
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
        if (phase is RoundPhase.MatchOver) {
            Text(
                text = stringResource(Res.string.game_match_winner, state.player(phase.winnerId)?.name.orEmpty()),
                fontFamily = ExtraBoldFont(),
                fontSize = 24.sp,
                color = ColorSuccess,
                textAlign = TextAlign.Center,
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

            RoundPhase.SecondRoll -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    text = stringResource(Res.string.game_keep),
                    onClick = listener::onKeepClick,
                    modifier = Modifier.weight(1f),
                    backgroundColor = ColorPrimaryLight,
                    textColor = ColorPrimary,
                )
                Button(
                    text = stringResource(Res.string.game_reroll_selected),
                    onClick = listener::onRerollClick,
                    enabled = state.currentTurn.selected.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                )
            }

            is RoundPhase.RoundOver -> Button(
                text = stringResource(Res.string.game_next_round),
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
private fun PassDeviceOverlay(passDevice: Game.PassDevice, onReady: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBlack.copy(alpha = 0.6f))
            // Swallow touches so the game underneath can't be used until "Ready".
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ColorSurface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.game_pass_last_roll, passDevice.previousPlayer.name),
                fontFamily = RegularFont(),
                fontSize = 14.sp,
                color = ColorTextSecondary,
            )
            DiceRow(dice = passDevice.previousDice, dieSize = 40.dp, spacing = 6.dp)
            Text(
                text = handName(HandEvaluator.evaluate(passDevice.previousDice).rank),
                fontFamily = BoldFont(),
                fontSize = 16.sp,
                color = ColorPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.game_pass_title),
                fontFamily = RegularFont(),
                fontSize = 14.sp,
                color = ColorTextSecondary,
            )
            Text(
                text = passDevice.nextPlayer.name,
                fontFamily = ExtraBoldFont(),
                fontSize = 28.sp,
                color = ColorTextPrimary,
                textAlign = TextAlign.Center,
            )
            Button(
                text = stringResource(Res.string.game_ready),
                onClick = onReady,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenSecondRollPreview() {
    AppTheme { GameScreen(viewData = PreviewHelper.gameSecondRoll()) }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenRoundOverPreview() {
    AppTheme { GameScreen(viewData = PreviewHelper.gameRoundOver()) }
}
