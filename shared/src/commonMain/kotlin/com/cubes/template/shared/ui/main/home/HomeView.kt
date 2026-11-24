package com.cubes.template.shared.ui.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.domain.ludo.Ludo
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.game_dice_duel
import com.cubes.template.shared.resources.game_dice_jack
import com.cubes.template.shared.resources.game_dice_poker
import com.cubes.template.shared.resources.game_ludo
import com.cubes.template.shared.resources.game_sevens
import com.cubes.template.shared.resources.game_risk_it
import com.cubes.template.shared.resources.game_straight_roll
import com.cubes.template.shared.resources.home_coming_soon
import com.cubes.template.shared.resources.home_title
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorFeltDark
import com.cubes.template.shared.ui.common.ColorFeltLight
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.PreviewHelper
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.views.DieFace
import com.cubes.template.shared.ui.common.views.GameRules
import com.cubes.template.shared.ui.common.views.LudoPawn
import com.cubes.template.shared.ui.common.views.PawnColor
import com.cubes.template.shared.ui.common.views.Toolbar
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val GRID_COLUMNS = 2
private val CARD_SPACING = 12.dp

@Composable
fun HomeView(viewModel: HomeViewModel) {
    HomeScreen(viewData = viewModel.viewData)
}

@Composable
fun HomeScreen(viewData: Home.ViewData) {
    val listener = viewData.listener
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        Toolbar(title = stringResource(Res.string.home_title))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(CARD_SPACING),
        ) {
            viewData.games.chunked(GRID_COLUMNS).forEach { rowGames ->
                Row(horizontalArrangement = Arrangement.spacedBy(CARD_SPACING)) {
                    rowGames.forEach { game ->
                        GameCardView(
                            game = game,
                            onClick = { listener.onGameClick(game) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(GRID_COLUMNS - rowGames.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            LudoCardView(onClick = { listener.onGameClick(Home.GameCard.LUDO) })
        }
    }
}

@Composable
private fun GameCardView(game: Home.GameCard, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .alpha(if (game.available) 1f else 0.55f)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.radialGradient(listOf(ColorFeltLight, ColorFeltDark)))
            .clickable(enabled = game.available, onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            game.dice.forEach { diceRow ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    diceRow.forEach { value -> DieFace(value = value, size = 28.dp) }
                }
            }
        }
        Text(
            text = stringResource(game.title),
            fontFamily = BoldFont(),
            fontSize = 17.sp,
            color = ColorWhite,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!game.available) {
            Text(
                text = stringResource(Res.string.home_coming_soon),
                fontFamily = RegularFont(),
                fontSize = 12.sp,
                color = ColorWhite,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun LudoCardView(onClick: () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val squareSide = (maxWidth - CARD_SPACING * (GRID_COLUMNS - 1)) / GRID_COLUMNS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(squareSide)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.radialGradient(listOf(ColorFeltLight, ColorFeltDark)))
                .clickable(onClick = onClick)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                LudoArtwork(pawnHeight = 44.dp, dieSize = 30.dp)
            }
            Text(
                text = stringResource(Res.string.game_ludo),
                fontFamily = BoldFont(),
                fontSize = 17.sp,
                color = ColorWhite,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun LudoArtwork(pawnHeight: Dp, dieSize: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(pawnHeight * 0.08f),
            modifier = Modifier.padding(bottom = dieSize * 0.55f),
        ) {
            PawnColor.defaultsFor(Ludo.MAX_PLAYERS).map { it.color }.forEach { color ->
                LudoPawn(color = color, height = pawnHeight)
            }
        }
        DieFace(value = 6, size = dieSize)
    }
}

internal val Home.GameCard.title: StringResource
    get() = when (this) {
        Home.GameCard.DICE_POKER -> Res.string.game_dice_poker
        Home.GameCard.DICE_JACK -> Res.string.game_dice_jack
        Home.GameCard.DICE_DUEL -> Res.string.game_dice_duel
        Home.GameCard.SEVENS -> Res.string.game_sevens
        Home.GameCard.RISK_IT -> Res.string.game_risk_it
        Home.GameCard.STRAIGHT_ROLL -> Res.string.game_straight_roll
        Home.GameCard.LUDO -> Res.string.game_ludo
    }

internal val Home.GameCard.rules: GameRules?
    get() = when (this) {
        Home.GameCard.DICE_POKER -> GameRules.DICE_POKER
        Home.GameCard.DICE_DUEL -> GameRules.DICE_DUEL
        Home.GameCard.SEVENS -> GameRules.SEVENS
        Home.GameCard.DICE_JACK -> GameRules.DICE_JACK
        Home.GameCard.STRAIGHT_ROLL -> GameRules.STRAIGHT_ROLL
        Home.GameCard.RISK_IT -> GameRules.RISK_IT
        Home.GameCard.LUDO -> GameRules.LUDO
    }

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AppTheme { HomeScreen(viewData = PreviewHelper.home()) }
}
