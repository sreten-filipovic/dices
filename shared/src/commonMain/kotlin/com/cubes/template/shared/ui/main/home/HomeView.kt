package com.cubes.template.shared.ui.main.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.cover_dice_duel
import com.cubes.template.shared.resources.cover_dice_jack
import com.cubes.template.shared.resources.cover_dice_poker
import com.cubes.template.shared.resources.cover_ludo
import com.cubes.template.shared.resources.cover_risk_it
import com.cubes.template.shared.resources.cover_sevens
import com.cubes.template.shared.resources.cover_straight_roll
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
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorFeltDark
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.PreviewHelper
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.ShapeLarge
import com.cubes.template.shared.ui.common.views.GameRules
import com.cubes.template.shared.ui.common.views.Toolbar
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val GRID_COLUMNS = 2
private val CARD_SPACING = 12.dp
private const val LUDO_COVER_RATIO = 1800f / 800f

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
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .alpha(if (game.available) 1f else 0.55f)
            .clip(ShapeLarge)
            .background(ColorFeltDark)
            .clickable(enabled = game.available, onClick = onClick),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Image(
            painter = painterResource(game.cover),
            contentDescription = stringResource(game.title),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (!game.available) {
            Text(
                text = stringResource(Res.string.home_coming_soon),
                fontFamily = RegularFont(),
                fontSize = 12.sp,
                color = ColorWhite,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
    }
}

@Composable
private fun LudoCardView(onClick: () -> Unit) {
    Image(
        painter = painterResource(Home.GameCard.LUDO.cover),
        contentDescription = stringResource(Res.string.game_ludo),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(LUDO_COVER_RATIO)
            .clip(ShapeLarge)
            .background(ColorFeltDark)
            .clickable(onClick = onClick),
    )
}

private val Home.GameCard.cover: DrawableResource
    get() = when (this) {
        Home.GameCard.DICE_POKER -> Res.drawable.cover_dice_poker
        Home.GameCard.DICE_JACK -> Res.drawable.cover_dice_jack
        Home.GameCard.DICE_DUEL -> Res.drawable.cover_dice_duel
        Home.GameCard.SEVENS -> Res.drawable.cover_sevens
        Home.GameCard.RISK_IT -> Res.drawable.cover_risk_it
        Home.GameCard.STRAIGHT_ROLL -> Res.drawable.cover_straight_roll
        Home.GameCard.LUDO -> Res.drawable.cover_ludo
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
