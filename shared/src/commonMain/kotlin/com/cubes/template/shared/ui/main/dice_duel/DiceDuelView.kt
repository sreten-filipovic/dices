package com.cubes.template.shared.ui.main.dice_duel

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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.domain.duel.DuelState
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.duel_play_again
import com.cubes.template.shared.resources.duel_results
import com.cubes.template.shared.resources.duel_winner
import com.cubes.template.shared.resources.duel_wins
import com.cubes.template.shared.resources.game_dice_duel
import com.cubes.template.shared.resources.game_done
import com.cubes.template.shared.resources.game_play_tiebreak
import com.cubes.template.shared.resources.game_tiebreak_next
import com.cubes.template.shared.resources.game_turn_of
import com.cubes.template.shared.resources.game_waiting
import com.cubes.template.shared.resources.roll
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorBlack
import com.cubes.template.shared.ui.common.ColorPrimary
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
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.common.views.DieFace
import com.cubes.template.shared.ui.common.views.GameRules
import com.cubes.template.shared.ui.common.views.RankingRow
import com.cubes.template.shared.ui.common.views.RankingSection
import com.cubes.template.shared.ui.common.views.Toolbar
import com.cubes.template.shared.ui.common.views.buttons.Button
import org.jetbrains.compose.resources.stringResource

private val CARD_DIE_SIZE = 28.dp

@Composable
fun DiceDuelView(viewModel: DiceDuelViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    DiceDuelScreen(viewData = viewData)
}

@Composable
fun DiceDuelScreen(viewData: DiceDuelUi.ViewData) {
    val listener = viewData.listener
    val state = viewData.state
    val blur by animateDpAsState(if (viewData.dialogOpen) 10.dp else 0.dp)

    Box(modifier = Modifier.fillMaxSize().background(ColorBackground)) {
        Column(modifier = Modifier.fillMaxSize().blur(blur)) {
            Toolbar(
                onBackClick = listener::onBackClick,
                rules = GameRules.DICE_DUEL,
                title = stringResource(Res.string.game_dice_duel),
            )

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlayersPanel(viewData)
                    if (viewData.showResult) ResultPanel(state) else TurnPanel(viewData)
                }

                Button(
                    text = stringResource(
                        when {
                            !viewData.showResult -> Res.string.roll
                            state.needsTiebreak -> Res.string.game_play_tiebreak
                            else -> Res.string.duel_play_again
                        }
                    ),
                    onClick = when {
                        !viewData.showResult -> listener::onRollClick
                        state.needsTiebreak -> listener::onTiebreakClick
                        else -> listener::onNextDuelClick
                    },
                    enabled = !viewData.dialogOpen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                )
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
private fun PlayersPanel(viewData: DiceDuelUi.ViewData) {
    val state = viewData.state
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.players.forEachIndexed { index, name ->
            val isWinner = viewData.showResult && index == state.winner
            val isCurrent = !viewData.dialogOpen && index == state.currentPlayer
            val hand = state.hands[index].takeUnless { viewData.rolling && index == viewData.tablePlayer }
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
                        text = stringResource(Res.string.duel_wins, state.wins[index]),
                        fontFamily = BoldFont(),
                        fontSize = 16.sp,
                        color = ColorPrimary,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.height(CARD_DIE_SIZE), verticalAlignment = Alignment.CenterVertically) {
                    if (hand != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            hand.forEach { value ->
                                DieFace(value = value, size = CARD_DIE_SIZE, style = viewData.dieStyleOf(index))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = hand.sum().toString(),
                            fontFamily = BoldFont(),
                            fontSize = 18.sp,
                            color = ColorTextPrimary,
                        )
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
        }
    }
}

@Composable
private fun TurnPanel(viewData: DiceDuelUi.ViewData) {
    val player = viewData.state.currentPlayer
    Text(
        text = if (player != null) stringResource(Res.string.game_turn_of, viewData.state.players[player]) else "",
        fontFamily = ExtraBoldFont(),
        fontSize = 28.sp,
        color = ColorTextPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
    )
}

@Composable
private fun ResultPanel(state: DuelState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeMedium)
            .background(ColorSurface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.needsTiebreak) {
            Text(
                text = stringResource(
                    Res.string.game_tiebreak_next,
                    state.leaders.joinToString { state.players[it] },
                ),
                fontFamily = BoldFont(),
                fontSize = 18.sp,
                color = ColorPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else if (state.players.size > 2) {
            RankingSection(title = stringResource(Res.string.duel_results)) {
                state.players.indices.sortedBy(state::placeOf).forEach { player ->
                    RankingRow(
                        place = state.placeOf(player),
                        name = state.players[player],
                        detail = null,
                        value = state.sumsOf(player).joinToString(" · "),
                    )
                }
            }
        } else {
            Text(
                text = stringResource(Res.string.duel_winner, state.winner?.let { state.players[it] }.orEmpty()),
                fontFamily = ExtraBoldFont(),
                fontSize = 24.sp,
                color = ColorSuccess,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun TableDialogContent(viewData: DiceDuelUi.ViewData) {
    val state = viewData.state
    val hand = state.hands[viewData.tablePlayer]
    DiceTable(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Text(
            text = state.players[viewData.tablePlayer],
            fontFamily = BoldFont(),
            fontSize = 22.sp,
            color = ColorWhite,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(state.diceCount) { index ->
                AnimatedDie(
                    value = hand?.getOrNull(index),
                    rollKey = 0,
                    size = 68.dp,
                    style = viewData.dieStyleOf(viewData.tablePlayer),
                    startDelayMillis = index * DieAnimation.STAGGER_MILLIS,
                    animateOnAppear = viewData.rolling,
                )
            }
        }
        Text(
            text = if (hand != null && !viewData.rolling) hand.sum().toString() else "",
            fontFamily = ExtraBoldFont(),
            fontSize = 34.sp,
            color = ColorWarning,
            modifier = Modifier.height(46.dp),
        )

        Button(
            text = stringResource(Res.string.game_done),
            onClick = viewData.listener::onDoneClick,
            enabled = viewData.dialogOpen && !viewData.rolling,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = ColorWhite,
            textColor = ColorPrimary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiceDuelScreenTurnPreview() {
    AppTheme { DiceDuelScreen(viewData = PreviewHelper.diceDuelTurn()) }
}

@Preview(showBackground = true)
@Composable
private fun DiceDuelScreenDialogPreview() {
    AppTheme { DiceDuelScreen(viewData = PreviewHelper.diceDuelDialog()) }
}

@Preview(showBackground = true)
@Composable
private fun DiceDuelScreenResultPreview() {
    AppTheme { DiceDuelScreen(viewData = PreviewHelper.diceDuelResult()) }
}

@Preview(showBackground = true)
@Composable
private fun DiceDuelScreenRankingPreview() {
    AppTheme { DiceDuelScreen(viewData = PreviewHelper.diceDuelRanking()) }
}
