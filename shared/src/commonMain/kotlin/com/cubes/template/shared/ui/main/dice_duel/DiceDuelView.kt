package com.cubes.template.shared.ui.main.dice_duel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.domain.duel.DiceDuel
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.duel_dice_count
import com.cubes.template.shared.resources.duel_draw
import com.cubes.template.shared.resources.duel_play_again
import com.cubes.template.shared.resources.duel_vs
import com.cubes.template.shared.resources.duel_winner
import com.cubes.template.shared.resources.duel_wins
import com.cubes.template.shared.resources.game_dice_duel
import com.cubes.template.shared.resources.game_turn_of
import com.cubes.template.shared.resources.roll
import com.cubes.template.shared.resources.setup_player_hint
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorPrimaryLight
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorWarning
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.PreviewHelper
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.views.AnimatedDie
import com.cubes.template.shared.ui.common.views.DiceTable
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.common.views.Toolbar
import com.cubes.template.shared.ui.common.views.buttons.Button
import org.jetbrains.compose.resources.stringResource

@Composable
fun DiceDuelView(viewModel: DiceDuelViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    DiceDuelScreen(viewData = viewData)
}

@Composable
fun DiceDuelScreen(viewData: DiceDuelUi.ViewData) {
    val listener = viewData.listener
    val state = viewData.state
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Toolbar(title = stringResource(Res.string.game_dice_duel), onBackClick = listener::onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Locked (dimmed) once the duel has started, so both players roll the same number of dice
            val canChangeDiceCount = state.canChangeDiceCount && !viewData.rolling
            Row(
                modifier = Modifier.fillMaxWidth().alpha(if (canChangeDiceCount) 1f else 0.5f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(Res.string.duel_dice_count),
                    fontFamily = BoldFont(),
                    fontSize = 18.sp,
                    color = ColorTextPrimary,
                    modifier = Modifier.weight(1f),
                )
                DiceDuel.DICE_COUNT_OPTIONS.forEach { count ->
                    val selected = count == state.diceCount
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (selected) ColorPrimary else ColorPrimaryLight)
                            .clickable(enabled = canChangeDiceCount) { listener.onDiceCountChanged(count) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = count.toString(),
                            color = if (selected) ColorWhite else ColorPrimary,
                            fontFamily = BoldFont(),
                            fontSize = 18.sp,
                        )
                    }
                }
            }

            DiceTable(modifier = Modifier.fillMaxWidth()) {
                PlayerPanel(viewData = viewData, player = 0)
                Text(
                    text = stringResource(Res.string.duel_vs),
                    fontFamily = ExtraBoldFont(),
                    fontSize = 16.sp,
                    color = ColorWhite.copy(alpha = 0.6f),
                )
                PlayerPanel(viewData = viewData, player = 1)
            }

            val currentPlayer = state.currentPlayer
            val winner = state.winner
            Text(
                text = when {
                    viewData.rolling -> ""
                    currentPlayer != null -> stringResource(Res.string.game_turn_of, playerName(currentPlayer))
                    winner != null -> stringResource(Res.string.duel_winner, playerName(winner))
                    else -> stringResource(Res.string.duel_draw)
                },
                fontFamily = if (viewData.showResult) ExtraBoldFont() else RegularFont(),
                fontSize = if (viewData.showResult) 24.sp else 16.sp,
                color = ColorTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(36.dp),
            )
        }

        Button(
            text = stringResource(if (viewData.showResult) Res.string.duel_play_again else Res.string.roll),
            onClick = if (viewData.showResult) listener::onNextDuelClick else listener::onRollClick,
            enabled = !viewData.rolling,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .navigationBarsPadding(),
        )
    }
}

@Composable
private fun PlayerPanel(viewData: DiceDuelUi.ViewData, player: Int) {
    val state = viewData.state
    val hand = state.hands[player]
    val settled = viewData.rollingPlayer != player
    val isTurn = !viewData.rolling && state.currentPlayer == player
    val isWinner = viewData.showResult && state.winner == player

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = playerName(player),
            fontFamily = BoldFont(),
            fontSize = 18.sp,
            color = if (isTurn || isWinner) ColorWarning else ColorWhite,
        )
        Text(
            text = stringResource(Res.string.duel_wins, viewData.wins[player]),
            fontFamily = RegularFont(),
            fontSize = 14.sp,
            color = ColorWhite.copy(alpha = 0.8f),
        )
        // Headroom for the dice to jump into
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(state.diceCount) { index ->
                AnimatedDie(
                    value = hand?.getOrNull(index),
                    rollKey = viewData.rollKeys[player],
                    size = 68.dp,
                    highlighted = isWinner,
                    startDelayMillis = index * DieAnimation.STAGGER_MILLIS,
                )
            }
        }
        Text(
            text = if (hand != null && settled) hand.sum().toString() else "",
            fontFamily = ExtraBoldFont(),
            fontSize = 30.sp,
            color = ColorWhite,
            modifier = Modifier.padding(top = 8.dp).height(40.dp),
        )
    }
}

@Composable
private fun playerName(player: Int) = stringResource(Res.string.setup_player_hint, player + 1)

@Preview(showBackground = true)
@Composable
private fun DiceDuelScreenTurnPreview() {
    AppTheme { DiceDuelScreen(viewData = PreviewHelper.diceDuelTurn()) }
}

@Preview(showBackground = true)
@Composable
private fun DiceDuelScreenResultPreview() {
    AppTheme { DiceDuelScreen(viewData = PreviewHelper.diceDuelResult()) }
}
