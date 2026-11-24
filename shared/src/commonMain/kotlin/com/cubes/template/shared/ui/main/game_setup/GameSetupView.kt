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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
import com.cubes.template.shared.resources.setup_pawn_color
import com.cubes.template.shared.resources.setup_player_settings
import com.cubes.template.shared.resources.setup_add_player
import com.cubes.template.shared.resources.setup_player_hint
import com.cubes.template.shared.resources.setup_players
import com.cubes.template.shared.resources.setup_remove_player
import com.cubes.template.shared.resources.setup_start
import com.cubes.template.shared.resources.setup_rounds
import com.cubes.template.shared.resources.setup_online_coming_soon
import com.cubes.template.shared.resources.setup_tab_local
import com.cubes.template.shared.resources.setup_tab_online
import com.cubes.template.shared.ui.common.ColorDivider
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
import com.cubes.template.shared.ui.common.views.LudoPawn
import com.cubes.template.shared.ui.common.views.PawnColor
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

private const val PAWN_COLOR_COLUMNS = 4

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

        ModeTabs(
            selected = viewData.mode,
            isOnlineAvailable = viewData.isOnlineAvailable,
            onSelect = { mode ->
                focusManager.clearFocus()
                listener.onModeSelected(mode)
            },
        )

        when (viewData.mode) {
            GameSetup.Mode.LOCAL -> LocalSetup(viewData, modifier = Modifier.weight(1f))
            GameSetup.Mode.ONLINE -> OnlineSetup(modifier = Modifier.weight(1f))
        }
    }

    viewData.settingsPlayer?.let { player ->
        PlayerSettingsDialog(
            playerName = viewData.names[player].trim()
                .ifEmpty { stringResource(Res.string.setup_player_hint, player + 1) },
            selected = viewData.dieStyles[player],
            onSelect = listener::onDieStyleSelected,
            pawnColors = if (viewData.choosesPawnColor) {
                PawnColorChoice(
                    selected = viewData.pawnColors[player],
                    taken = viewData.pawnColorsTakenByOthers(player),
                    onSelect = listener::onPawnColorSelected,
                )
            } else {
                null
            },
            onDismiss = listener::onSettingsDismiss,
        )
    }
}

@Composable
private fun ModeTabs(
    selected: GameSetup.Mode,
    isOnlineAvailable: Boolean,
    onSelect: (GameSetup.Mode) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(ColorSurface)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            GameSetup.Mode.entries.forEach { mode ->
                ModeTab(
                    text = stringResource(
                        when (mode) {
                            GameSetup.Mode.LOCAL -> Res.string.setup_tab_local
                            GameSetup.Mode.ONLINE -> Res.string.setup_tab_online
                        }
                    ),
                    isSelected = mode == selected,
                    isEnabled = mode == GameSetup.Mode.LOCAL || isOnlineAvailable,
                    onClick = { onSelect(mode) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(ColorDivider))
    }
}

@Composable
private fun ModeTab(
    text: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A disabled tab stays clickable so the view model can explain why it is unavailable.
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .alpha(if (isEnabled) 1f else 0.4f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            fontFamily = if (isSelected) BoldFont() else SemiBoldFont(),
            fontSize = 16.sp,
            color = if (isSelected) ColorPrimary else ColorTextSecondary,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(if (isSelected) ColorPrimary else ColorTransparent)
        )
    }
}

@Composable
private fun OnlineSetup(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(Res.string.setup_online_coming_soon),
            fontFamily = SemiBoldFont(),
            fontSize = 16.sp,
            color = ColorTextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LocalSetup(viewData: GameSetup.ViewData, modifier: Modifier = Modifier) {
    val listener = viewData.listener
    val focusManager = LocalFocusManager.current
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle(stringResource(Res.string.setup_players))

            val pawnColors = if (viewData.choosesPawnColor) viewData.pawnColors else null
            viewData.names.forEachIndexed { index, name ->
                val pawnColor = pawnColors?.getOrNull(index)?.color
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { listener.onNameChanged(index, it) },
                        placeholder = { Text(stringResource(Res.string.setup_player_hint, index + 1)) },
                        leadingIcon = pawnColor?.let { { LudoPawn(color = it, height = 28.dp) } },
                        colors = if (pawnColor != null) {
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = pawnColor,
                                unfocusedBorderColor = pawnColor,
                                cursorColor = pawnColor,
                            )
                        } else {
                            OutlinedTextFieldDefaults.colors()
                        },
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
            } else if (viewData.choosesRounds) {
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

private class PawnColorChoice(
    val selected: PawnColor,
    val taken: Set<PawnColor>,
    val onSelect: (PawnColor) -> Unit,
)

@Composable
private fun PlayerSettingsDialog(
    playerName: String,
    selected: DieStyle,
    onSelect: (DieStyle) -> Unit,
    pawnColors: PawnColorChoice?,
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
            PlayerSettingsCard(playerName, selected, onSelect, pawnColors, onDismiss)
        }
    }
}

@Composable
private fun PlayerSettingsCard(
    playerName: String,
    selected: DieStyle,
    onSelect: (DieStyle) -> Unit,
    pawnColors: PawnColorChoice?,
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
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                        .padding(6.dp),
                ) {
                    DieFace(value = 5, size = 40.dp, style = style)
                }
            }
        }
        if (pawnColors != null) {
            Text(
                text = stringResource(Res.string.setup_pawn_color),
                fontFamily = SemiBoldFont(),
                fontSize = 14.sp,
                color = ColorTextSecondary,
            )
            PawnColorGrid(pawnColors)
        }
        Button(
            text = stringResource(Res.string.ok),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PawnColorGrid(choice: PawnColorChoice) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PawnColor.entries.chunked(PAWN_COLOR_COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { color ->
                    val isSelected = color == choice.selected
                    val isTaken = color in choice.taken
                    Box(
                        modifier = Modifier
                            .alpha(if (isTaken) 0.25f else 1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) color.color.copy(alpha = 0.2f) else ColorTransparent)
                            .border(
                                width = 2.dp,
                                color = if (isSelected) color.color else ColorTransparent,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable(enabled = !isTaken) { choice.onSelect(color) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LudoPawn(color = color.color, height = 40.dp)
                    }
                }
            }
        }
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
    AppTheme {
        PlayerSettingsCard(
            playerName = "Ana",
            selected = DieStyle.BLACK,
            onSelect = {},
            pawnColors = PawnColorChoice(
                selected = PawnColor.PURPLE,
                taken = setOf(PawnColor.RED, PawnColor.GREEN),
                onSelect = {},
            ),
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameSetupScreenPreview() {
    AppTheme { GameSetupScreen(viewData = PreviewHelper.gameSetup()) }
}
