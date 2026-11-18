package com.cubes.template.shared.ui.main.game_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.setup_add_player
import com.cubes.template.shared.resources.setup_player_hint
import com.cubes.template.shared.resources.setup_players
import com.cubes.template.shared.resources.setup_remove_player
import com.cubes.template.shared.resources.setup_start
import com.cubes.template.shared.resources.setup_rounds
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorPrimaryLight
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.PreviewHelper
import com.cubes.template.shared.ui.common.SemiBoldFont
import com.cubes.template.shared.ui.common.views.ScreenTitle
import com.cubes.template.shared.ui.common.views.Toolbar
import com.cubes.template.shared.ui.common.views.buttons.Button
import com.cubes.template.shared.ui.main.home.title
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameSetupView(viewModel: GameSetupViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    GameSetupScreen(viewData = viewData)
}

@Composable
fun GameSetupScreen(viewData: GameSetup.ViewData) {
    val listener = viewData.listener
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .imePadding()
    ) {
        Toolbar(onBackClick = listener::onBackClick)

        // The green bar stays on top; the title, content and buttons scroll under it
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            ScreenTitle(title = stringResource(viewData.game.title))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SectionTitle(stringResource(Res.string.setup_players))

                viewData.names.forEachIndexed { index, name ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { listener.onNameChanged(index, it) },
                            placeholder = { Text(stringResource(Res.string.setup_player_hint, index + 1)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        if (viewData.canRemovePlayer) {
                            TextButton(onClick = { listener.onRemovePlayerClick(index) }) {
                                Text(stringResource(Res.string.setup_remove_player), color = ColorPrimary)
                            }
                        }
                    }
                }

                if (viewData.canAddPlayer) {
                    TextButton(onClick = listener::onAddPlayerClick) {
                        Text(
                            text = stringResource(Res.string.setup_add_player),
                            color = ColorPrimary,
                            fontFamily = SemiBoldFont(),
                        )
                    }
                }

                SectionTitle(stringResource(Res.string.setup_rounds))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GameSetup.ROUND_OPTIONS.forEach { value ->
                        val selected = value == viewData.totalRounds
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (selected) ColorPrimary else ColorPrimaryLight)
                                .clickable { listener.onTotalRoundsChanged(value) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = value.toString(),
                                color = if (selected) ColorWhite else ColorPrimary,
                                fontFamily = BoldFont(),
                                fontSize = 18.sp,
                            )
                        }
                    }
                }
            }

            Button(
                text = stringResource(Res.string.setup_start),
                onClick = listener::onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .navigationBarsPadding(),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, fontFamily = BoldFont(), fontSize = 18.sp, color = ColorTextPrimary)
}

@Preview(showBackground = true)
@Composable
private fun GameSetupScreenPreview() {
    AppTheme { GameSetupScreen(viewData = PreviewHelper.gameSetup()) }
}
