package com.cubes.template.shared.ui.main.ludo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.domain.ludo.Ludo
import com.cubes.template.shared.domain.ludo.LudoPhase
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.duel_play_again
import com.cubes.template.shared.resources.duel_winner
import com.cubes.template.shared.resources.game_home
import com.cubes.template.shared.resources.game_ludo
import com.cubes.template.shared.resources.ludo_finished
import com.cubes.template.shared.resources.ludo_hint_finished
import com.cubes.template.shared.resources.ludo_place
import com.cubes.template.shared.resources.ludo_hint_need_six
import com.cubes.template.shared.resources.ludo_hint_no_moves
import com.cubes.template.shared.resources.ludo_hint_pick_pawn
import com.cubes.template.shared.resources.ludo_hint_roll
import com.cubes.template.shared.resources.ludo_hint_roll_again
import com.cubes.template.shared.resources.ludo_turn
import com.cubes.template.shared.resources.roll
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorBlack
import com.cubes.template.shared.ui.common.ColorFeltDark
import com.cubes.template.shared.ui.common.ColorGold
import com.cubes.template.shared.ui.common.ColorSurface
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.ColorTransparent
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.SemiBoldFont
import com.cubes.template.shared.ui.common.clickableNoRipple
import com.cubes.template.shared.ui.common.views.AnimatedDie
import com.cubes.template.shared.ui.common.views.DiceTable
import com.cubes.template.shared.ui.common.views.GameRules
import com.cubes.template.shared.ui.common.views.LudoPawn
import com.cubes.template.shared.ui.common.views.Toolbar
import com.cubes.template.shared.ui.common.views.buttons.Button
import org.jetbrains.compose.resources.stringResource

@Composable
fun LudoView(viewModel: LudoViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    LudoScreen(viewData = viewData)
}

@Composable
fun LudoScreen(viewData: LudoUi.ViewData) {
    val listener = viewData.listener
    val state = viewData.state
    val ranking = (state.phase as? LudoPhase.GameOver)?.ranking?.takeIf { !viewData.moving }
    val lastRanking = remember { mutableStateOf<List<Int>?>(null) }
    if (ranking != null) lastRanking.value = ranking

    Box(modifier = Modifier.fillMaxSize().background(ColorBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Toolbar(
                onBackClick = listener::onBackClick,
                rules = GameRules.LUDO,
                title = stringResource(Res.string.game_ludo),
            )
            PlayersRow(viewData)
            BoxWithConstraints(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                val side = minOf(maxWidth, maxHeight)
                LudoBoard(
                    state = state,
                    pawnColors = viewData.pawnColors,
                    movable = viewData.movable,
                    onPawnClick = listener::onPawnClick,
                    modifier = Modifier.size(side),
                )
            }
            RollPanel(viewData)
        }

        AnimatedVisibility(visible = ranking != null, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ColorBlack.copy(alpha = 0.45f))
                    .clickableNoRipple {},
            )
        }
        AnimatedVisibility(
            visible = ranking != null,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
        ) {
            lastRanking.value?.let { ResultsCard(viewData, it) }
        }
    }
}

@Composable
private fun PlayersRow(viewData: LudoUi.ViewData) {
    val state = viewData.state
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.players.forEachIndexed { index, player ->
            val isCurrent = index == state.currentPlayer && state.phase !is LudoPhase.GameOver
            val color = viewData.colorOf(index)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isCurrent) color.copy(alpha = 0.22f) else ColorSurface)
                    .border(2.dp, if (isCurrent) color else ColorTransparent, RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LudoPawn(color = color, height = 26.dp)
                Column {
                    Text(
                        text = player.name,
                        fontFamily = if (isCurrent) BoldFont() else SemiBoldFont(),
                        fontSize = 13.sp,
                        color = ColorTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val place = state.placeOf(index)
                    Text(
                        text = if (place != null) {
                            stringResource(Res.string.ludo_place, place)
                        } else {
                            stringResource(Res.string.ludo_finished, state.finishedCount(index), Ludo.PAWNS)
                        },
                        fontFamily = RegularFont(),
                        fontSize = 11.sp,
                        color = ColorTextSecondary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun RollPanel(viewData: LudoUi.ViewData) {
    val listener = viewData.listener
    val player = viewData.state.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorSurface)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AnimatedDie(
            value = viewData.dieValue,
            rollKey = viewData.rollKey,
            size = 60.dp,
            style = viewData.currentDieStyle,
            onClick = if (viewData.canRoll) listener::onRollClick else null,
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(viewData.colorOf(viewData.state.currentPlayer))
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.ludo_turn, player.name),
                    fontFamily = BoldFont(),
                    fontSize = 16.sp,
                    color = ColorTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = hintText(viewData),
                fontFamily = RegularFont(),
                fontSize = 13.sp,
                color = ColorTextSecondary,
                minLines = 2,
                maxLines = 2,
            )
        }
        Button(
            text = stringResource(Res.string.roll),
            onClick = listener::onRollClick,
            enabled = viewData.canRoll,
            backgroundColor = ColorFeltDark,
            modifier = Modifier.width(96.dp),
        )
    }
}

@Composable
private fun hintText(viewData: LudoUi.ViewData): String = when (viewData.hint) {
    LudoUi.Hint.ROLL -> stringResource(Res.string.ludo_hint_roll)
    LudoUi.Hint.NEED_SIX -> stringResource(Res.string.ludo_hint_need_six, viewData.state.rollsLeft)
    LudoUi.Hint.ROLL_AGAIN -> stringResource(Res.string.ludo_hint_roll_again)
    LudoUi.Hint.PICK_PAWN -> stringResource(Res.string.ludo_hint_pick_pawn, viewData.dieValue ?: 0)
    LudoUi.Hint.NO_MOVES -> stringResource(Res.string.ludo_hint_no_moves)
    LudoUi.Hint.PLAYER_FINISHED -> viewData.finishedPlayer?.let { player ->
        stringResource(
            Res.string.ludo_hint_finished,
            viewData.state.players[player].name,
            viewData.state.placeOf(player) ?: 0,
        )
    }.orEmpty()
    LudoUi.Hint.NONE -> ""
}

@Composable
private fun ResultsCard(viewData: LudoUi.ViewData, ranking: List<Int>) {
    val listener = viewData.listener
    val player = viewData.state.players[ranking.first()]
    DiceTable(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(Ludo.PAWNS) { LudoPawn(color = viewData.colorOf(ranking.first()), height = 52.dp) }
        }
        Text(
            text = stringResource(Res.string.duel_winner, player.name),
            fontFamily = ExtraBoldFont(),
            fontSize = 26.sp,
            color = ColorWhite,
            textAlign = TextAlign.Center,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ColorBlack.copy(alpha = 0.18f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ranking.forEachIndexed { index, playerIndex ->
                val ranked = viewData.state.players[playerIndex]
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${index + 1}.",
                        fontFamily = ExtraBoldFont(),
                        fontSize = 18.sp,
                        color = if (index == 0) ColorGold else ColorWhite,
                        modifier = Modifier.width(28.dp),
                    )
                    LudoPawn(color = viewData.colorOf(playerIndex), height = 26.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = ranked.name,
                        fontFamily = if (index == 0) BoldFont() else SemiBoldFont(),
                        fontSize = 17.sp,
                        color = ColorWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Button(
            text = stringResource(Res.string.duel_play_again),
            onClick = listener::onPlayAgainClick,
            backgroundColor = ColorWhite,
            textColor = ColorFeltDark,
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = listener::onBackClick) {
            Text(
                text = stringResource(Res.string.game_home),
                fontFamily = SemiBoldFont(),
                fontSize = 16.sp,
                color = ColorWhite,
            )
        }
    }
}

private val previewListener = object : LudoViewListener {
    override fun onRollClick() = Unit
    override fun onPawnClick(pawn: Int) = Unit
    override fun onPlayAgainClick() = Unit
    override fun onBackClick() = Unit
}

@Preview
@Composable
private fun LudoScreenPreview() {
    AppTheme {
        val state = Ludo.newGame(listOf("Ana", "Marko", "Iva"))
        LudoScreen(
            LudoUi.ViewData(
                state = state.copy(pawns = listOf(listOf(0, 9, Ludo.HOME, 41), state.pawns[1], state.pawns[2])),
                dieValue = 4,
                listener = previewListener,
            )
        )
    }
}

@Preview
@Composable
private fun LudoWinnerPreview() {
    AppTheme {
        val state = Ludo.newGame(listOf("Ana", "Marko"))
        LudoScreen(
            LudoUi.ViewData(
                state = state.copy(
                    pawns = listOf(listOf(40, 41, 42, 43), state.pawns[1]),
                    ranking = listOf(0, 1),
                    phase = LudoPhase.GameOver(listOf(0, 1)),
                ),
                listener = previewListener,
            )
        )
    }
}
