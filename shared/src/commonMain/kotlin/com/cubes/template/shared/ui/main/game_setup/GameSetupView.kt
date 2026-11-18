package com.cubes.template.shared.ui.main.game_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.duel_dice_count
import com.cubes.template.shared.resources.ic_delete
import com.cubes.template.shared.resources.ic_settings
import com.cubes.template.shared.resources.ok
import com.cubes.template.shared.resources.setup_dice_color
import com.cubes.template.shared.resources.setup_player_settings
import com.cubes.template.shared.resources.setup_add_player
import com.cubes.template.shared.resources.setup_player_hint
import com.cubes.template.shared.resources.setup_players
import com.cubes.template.shared.resources.setup_remove_player
import com.cubes.template.shared.resources.setup_start
import com.cubes.template.shared.resources.setup_rounds
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorAccent
import com.cubes.template.shared.ui.common.ColorBlack
import com.cubes.template.shared.ui.common.ColorSurface
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.ColorTransparent
import com.cubes.template.shared.ui.common.clearFocusOnTapOutside
import com.cubes.template.shared.ui.common.clickableNoRipple
import com.cubes.template.shared.ui.common.popup.popupDialogProperties
import com.cubes.template.shared.ui.common.views.DieFace
import com.cubes.template.shared.ui.common.views.DieStyle
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorPrimaryLight
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.PreviewHelper
import com.cubes.template.shared.ui.common.SemiBoldFont
import com.cubes.template.shared.ui.common.views.Toolbar
import com.cubes.template.shared.ui.common.views.buttons.Button
import com.cubes.template.shared.ui.main.home.rules
import com.cubes.template.shared.ui.main.home.title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameSetupView(viewModel: GameSetupViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    GameSetupScreen(viewData = viewData)
}

@Composable
fun GameSetupScreen(viewData: GameSetup.ViewData) {
    val listener = viewData.listener
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .imePadding()
            .clearFocusOnTapOutside()
    ) {
        Toolbar(
            onBackClick = listener::onBackClick,
            rules = viewData.game.rules,
            title = stringResource(viewData.game.title),
        )

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
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
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            listener.onSettingsClick(index)
                        }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_settings),
                                contentDescription = stringResource(Res.string.setup_player_settings),
                                tint = ColorPrimary,
                            )
                        }
                        if (viewData.canRemovePlayer) {
                            IconButton(onClick = { listener.onRemovePlayerClick(index) }) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_delete),
                                    contentDescription = stringResource(Res.string.setup_remove_player),
                                    tint = ColorAccent,
                                )
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

                if (viewData.choosesDiceCount) {
                    SectionTitle(stringResource(Res.string.duel_dice_count))
                    OptionChips(
                        options = GameSetup.DICE_COUNT_OPTIONS,
                        selected = viewData.diceCount,
                        onSelect = listener::onDiceCountChanged,
                    )
                } else {
                    SectionTitle(stringResource(Res.string.setup_rounds))
                    OptionChips(
                        options = GameSetup.ROUND_OPTIONS,
                        selected = viewData.totalRounds,
                        onSelect = listener::onTotalRoundsChanged,
                    )
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

    viewData.settingsPlayer?.let { player ->
        PlayerSettingsDialog(
            playerName = viewData.names[player].trim()
                .ifEmpty { stringResource(Res.string.setup_player_hint, player + 1) },
            selected = viewData.dieStyles[player],
            onSelect = listener::onDieStyleSelected,
            onDismiss = listener::onSettingsDismiss,
        )
    }
}

@Composable
private fun PlayerSettingsDialog(
    playerName: String,
    selected: DieStyle,
    onSelect: (DieStyle) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = popupDialogProperties()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorBlack.copy(alpha = 0.5f))
                .clickableNoRipple(onDismiss)
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            PlayerSettingsCard(playerName, selected, onSelect, onDismiss)
        }
    }
}

@Composable
private fun PlayerSettingsCard(
    playerName: String,
    selected: DieStyle,
    onSelect: (DieStyle) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ColorSurface)
            .clickableNoRipple {}
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = playerName,
            fontFamily = BoldFont(),
            fontSize = 22.sp,
            color = ColorTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(Res.string.setup_dice_color),
            fontFamily = SemiBoldFont(),
            fontSize = 14.sp,
            color = ColorTextSecondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DieStyle.entries.forEach { style ->
                val isSelected = style == selected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) ColorPrimaryLight else ColorTransparent)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) ColorPrimary else ColorTransparent,
                            shape = RoundedCornerShape(14.dp),
                        )
                        .clickable { onSelect(style) }
                        .padding(8.dp),
                ) {
                    DieFace(value = 5, size = 48.dp, style = style)
                }
            }
        }
        Button(
            text = stringResource(Res.string.ok),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun OptionChips(options: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { value ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) ColorPrimary else ColorPrimaryLight)
                    .clickable { onSelect(value) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = value.toString(),
                    color = if (isSelected) ColorWhite else ColorPrimary,
                    fontFamily = BoldFont(),
                    fontSize = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, fontFamily = BoldFont(), fontSize = 18.sp, color = ColorTextPrimary)
}

@Preview
@Composable
private fun PlayerSettingsCardPreview() {
    AppTheme { PlayerSettingsCard(playerName = "Ana", selected = DieStyle.BLACK, onSelect = {}, onDismiss = {}) }
}

@Preview(showBackground = true)
@Composable
private fun GameSetupScreenPreview() {
    AppTheme { GameSetupScreen(viewData = PreviewHelper.gameSetup()) }
}
