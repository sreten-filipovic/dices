package com.cubes.template.shared.ui.main.online.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.data.online.RoomSummary
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.online_code_hint
import com.cubes.template.shared.resources.online_create_button
import com.cubes.template.shared.resources.online_create_title
import com.cubes.template.shared.resources.online_join_button
import com.cubes.template.shared.resources.online_join_title
import com.cubes.template.shared.resources.online_no_rooms
import com.cubes.template.shared.resources.online_open_rooms
import com.cubes.template.shared.resources.online_players_count
import com.cubes.template.shared.resources.online_request_button
import com.cubes.template.shared.resources.online_room_host
import com.cubes.template.shared.resources.online_room_players
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorPrimaryLight
import com.cubes.template.shared.ui.common.ColorSurface
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.SemiBoldFont
import com.cubes.template.shared.ui.common.views.LoadingIndicator
import com.cubes.template.shared.ui.common.views.buttons.LoadingButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnlineLobbyView(viewModel: OnlineLobbyViewModel, modifier: Modifier = Modifier) {
    val viewData by viewModel.viewData.collectAsState()
    LaunchedEffect(viewModel) { viewModel.keepRefreshing() }
    OnlineLobbyContent(viewData, modifier)
}

@Composable
fun OnlineLobbyContent(viewData: OnlineLobby.ViewData, modifier: Modifier = Modifier) {
    val listener = viewData.listener
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LobbyCard(title = stringResource(Res.string.online_create_title)) {
            Text(
                text = stringResource(Res.string.online_players_count),
                fontFamily = SemiBoldFont(),
                fontSize = 14.sp,
                color = ColorTextSecondary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                viewData.playerOptions.forEach { count ->
                    CountChip(
                        count = count,
                        selected = count == viewData.maxPlayers,
                        onClick = { listener.onMaxPlayersSelected(count) },
                    )
                }
            }
            LoadingButton(
                text = stringResource(Res.string.online_create_button),
                onClick = listener::onCreateRoomClick,
                isLoading = viewData.busy == OnlineLobby.Busy.Creating,
                enabled = viewData.busy == null,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        LobbyCard(title = stringResource(Res.string.online_join_title)) {
            OutlinedTextField(
                value = viewData.code,
                onValueChange = listener::onCodeChanged,
                label = { Text(stringResource(Res.string.online_code_hint)) },
                isError = viewData.codeError != null,
                supportingText = viewData.codeError?.let { { Text(it) } },
                singleLine = true,
                enabled = viewData.busy == null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions(onGo = {
                    focusManager.clearFocus()
                    listener.onJoinByCodeClick()
                }),
                modifier = Modifier.fillMaxWidth(),
            )
            LoadingButton(
                text = stringResource(Res.string.online_join_button),
                onClick = {
                    focusManager.clearFocus()
                    listener.onJoinByCodeClick()
                },
                isLoading = viewData.busy == OnlineLobby.Busy.JoiningByCode,
                enabled = viewData.canJoinByCode,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Text(
            text = stringResource(Res.string.online_open_rooms),
            fontFamily = BoldFont(),
            fontSize = 18.sp,
            color = ColorTextPrimary,
        )
        when {
            viewData.isLoadingRooms -> LoadingIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp))
            viewData.rooms.isEmpty() -> Text(
                text = stringResource(Res.string.online_no_rooms),
                fontFamily = RegularFont(),
                fontSize = 14.sp,
                color = ColorTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            )
            else -> viewData.rooms.forEach { room ->
                RoomRow(
                    room = room,
                    isJoining = viewData.busy == OnlineLobby.Busy.JoiningRoom(room.id),
                    enabled = viewData.busy == null && room.playerCount < room.maxPlayers,
                    onClick = { listener.onRoomClick(room.id) },
                )
            }
        }
    }
}

@Composable
private fun LobbyCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ColorSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = title, fontFamily = BoldFont(), fontSize = 18.sp, color = ColorTextPrimary)
        content()
    }
}

@Composable
private fun CountChip(count: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) ColorPrimary else ColorPrimaryLight)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            fontFamily = BoldFont(),
            fontSize = 17.sp,
            color = if (selected) ColorWhite else ColorPrimary,
        )
    }
}

@Composable
private fun RoomRow(room: RoomSummary, isJoining: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.online_room_host, room.hostName),
                fontFamily = BoldFont(),
                fontSize = 16.sp,
                color = ColorTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(Res.string.online_room_players, room.playerCount, room.maxPlayers),
                fontFamily = RegularFont(),
                fontSize = 13.sp,
                color = ColorTextSecondary,
            )
        }
        LoadingButton(
            text = stringResource(Res.string.online_request_button),
            onClick = onClick,
            isLoading = isJoining,
            enabled = enabled,
            modifier = Modifier.width(140.dp),
        )
    }
}

@Preview
@Composable
private fun OnlineLobbyPreview() {
    AppTheme {
        OnlineLobbyContent(
            OnlineLobby.ViewData(
                playerOptions = listOf(2, 3, 4),
                maxPlayers = 4,
                isLoadingRooms = false,
                rooms = listOf(RoomSummary("1", "K7M2QX", "ana", 4, 2), RoomSummary("2", "P3ZT8R", "marko", 2, 1)),
                listener = object : OnlineLobbyViewListener {
                    override fun onMaxPlayersSelected(count: Int) = Unit
                    override fun onCreateRoomClick() = Unit
                    override fun onCodeChanged(code: String) = Unit
                    override fun onJoinByCodeClick() = Unit
                    override fun onRoomClick(roomId: String) = Unit
                },
            )
        )
    }
}
