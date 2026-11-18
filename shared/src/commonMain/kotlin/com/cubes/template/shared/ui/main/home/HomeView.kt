package com.cubes.template.shared.ui.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.game_dice_duel
import com.cubes.template.shared.resources.game_dice_jack
import com.cubes.template.shared.resources.game_dice_poker
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
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.PreviewHelper
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.views.AnimatedDie
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.common.views.DieFace
import com.cubes.template.shared.ui.common.views.GameRules
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private const val GRID_COLUMNS = 2

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
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Tapping the logo dice rolls them again (they always land on five sixes)
        var logoRollKey by remember { mutableStateOf(0) }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                // Headroom so the jumping dice are not clipped by the scroll container
                .padding(top = 40.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { logoRollKey++ },
        ) {
            repeat(5) { index ->
                AnimatedDie(
                    value = 6,
                    rollKey = logoRollKey,
                    size = 44.dp,
                    highlighted = true,
                    startDelayMillis = index * DieAnimation.STAGGER_MILLIS,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.home_title),
            fontFamily = ExtraBoldFont(),
            fontSize = 34.sp,
            color = ColorTextPrimary,
        )
        Spacer(Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            viewData.games.chunked(GRID_COLUMNS).forEach { rowGames ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowGames.forEach { game ->
                        GameCardView(
                            game = game,
                            onClick = { listener.onGameClick(game) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Keep the cards of an incomplete last row the same size
                    repeat(GRID_COLUMNS - rowGames.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** Square felt card with the game's dice artwork and name; dimmed when the game is not playable yet. */
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

internal val Home.GameCard.title: StringResource
    get() = when (this) {
        Home.GameCard.DICE_POKER -> Res.string.game_dice_poker
        Home.GameCard.DICE_JACK -> Res.string.game_dice_jack
        Home.GameCard.DICE_DUEL -> Res.string.game_dice_duel
        Home.GameCard.SEVENS -> Res.string.game_sevens
        Home.GameCard.RISK_IT -> Res.string.game_risk_it
        Home.GameCard.STRAIGHT_ROLL -> Res.string.game_straight_roll
    }

/** Rules shown from the toolbar of the game's screens; `null` for games that are not playable yet. */
internal val Home.GameCard.rules: GameRules?
    get() = when (this) {
        Home.GameCard.DICE_POKER -> GameRules.DICE_POKER
        Home.GameCard.DICE_DUEL -> GameRules.DICE_DUEL
        Home.GameCard.SEVENS -> GameRules.SEVENS
        Home.GameCard.DICE_JACK,
        Home.GameCard.RISK_IT,
        Home.GameCard.STRAIGHT_ROLL -> null
    }

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AppTheme { HomeScreen(viewData = PreviewHelper.home()) }
}
