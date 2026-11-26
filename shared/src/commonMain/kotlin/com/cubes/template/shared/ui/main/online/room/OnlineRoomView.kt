package com.cubes.template.shared.ui.main.online.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.data.online.MemberStatus
import com.cubes.template.shared.data.online.OnlineGame
import com.cubes.template.shared.data.online.Room
import com.cubes.template.shared.data.online.RoomMember
import com.cubes.template.shared.data.online.RoomStatus
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.online_accept
import com.cubes.template.shared.resources.online_cancel_request
import com.cubes.template.shared.resources.online_copied
import com.cubes.template.shared.resources.online_copy_code
import com.cubes.template.shared.resources.online_decline
import com.cubes.template.shared.resources.online_empty_seat
import com.cubes.template.shared.resources.online_host_badge
import com.cubes.template.shared.resources.online_leave_room
import com.cubes.template.shared.resources.online_players_title
import com.cubes.template.shared.resources.online_remove
import com.cubes.template.shared.resources.online_requests_title
import com.cubes.template.shared.resources.online_room_code_hint
import com.cubes.template.shared.resources.online_room_code_label
import com.cubes.template.shared.resources.online_room_title
import com.cubes.template.shared.resources.online_start_button
import com.cubes.template.shared.resources.online_start_hint
import com.cubes.template.shared.resources.online_waiting_host
import com.cubes.template.shared.resources.online_waiting_start
import com.cubes.template.shared.resources.online_you_badge
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorAccent
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.common.ColorDivider
import com.cubes.template.shared.ui.common.ColorFeltDark
import com.cubes.template.shared.ui.common.ColorFeltLight
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorPrimaryLight
import com.cubes.template.shared.ui.common.ColorSurface
import com.cubes.template.shared.ui.common.ColorTextPrimary
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.SemiBoldFont
import com.cubes.template.shared.ui.common.ShapeLarge
import com.cubes.template.shared.ui.common.ShapeMedium
import com.cubes.template.shared.ui.common.ShapeSmall
import com.cubes.template.shared.ui.common.views.LoadingIndicator
import com.cubes.template.shared.ui.common.views.DieFace
import com.cubes.template.shared.ui.common.views.LudoPawn
import com.cubes.template.shared.ui.common.views.PawnColor
import com.cubes.template.shared.ui.common.views.Toolbar
import com.cubes.template.shared.ui.common.views.buttons.Button
import com.cubes.template.shared.ui.common.views.buttons.LoadingButton
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnlineRoomView(viewModel: OnlineRoomViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    OnlineRoomScreen(viewData)
}

@Composable
fun OnlineRoomScreen(viewData: OnlineRoom.ViewData) {
    val listener = viewData.listener
    val room = viewData.room
    Column(modifier = Modifier.fillMaxSize().background(ColorBackground)) {
        Toolbar(
            onBackClick = listener::onLeaveClick,
            title = room?.let { stringResource(Res.string.online_room_title, it.code) },
        )
        if (room == null) {
            LoadingIndicator(modifier = Modifier.fillMaxSize())
            return@Column
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (viewData.isPending) {
                StatusCard(stringResource(Res.string.online_waiting_host, viewData.hostName))
            } else {
                CodeCard(room.code)
                if (!viewData.isHost) {
                    StatusCard(stringResource(Res.string.online_waiting_start, viewData.hostName))
                }
            }
            PlayersCard(viewData, room)
            if (viewData.isHost && room.pending.isNotEmpty()) {
                RequestsCard(viewData, room.pending)
            }
        }
        BottomActions(viewData)
    }
}

@Composable
private fun CodeCard(code: String) {
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(COPIED_MILLIS)
            copied = false
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLarge)
            .background(Brush.radialGradient(listOf(ColorFeltLight, ColorFeltDark)))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.online_room_code_label),
            fontFamily = SemiBoldFont(),
            fontSize = 14.sp,
            color = ColorWhite.copy(alpha = 0.8f),
        )
        Text(
            text = code.toList().joinToString(" "),
            fontFamily = ExtraBoldFont(),
            fontSize = 34.sp,
            color = ColorWhite,
        )
        Button(
            text = stringResource(if (copied) Res.string.online_copied else Res.string.online_copy_code),
            onClick = {
                clipboard.setText(AnnotatedString(code))
                copied = true
            },
            backgroundColor = ColorWhite,
            textColor = ColorFeltDark,
            modifier = Modifier.width(160.dp),
        )
        Text(
            text = stringResource(Res.string.online_room_code_hint),
            fontFamily = RegularFont(),
            fontSize = 13.sp,
            color = ColorWhite.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatusCard(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeMedium)
            .background(ColorPrimaryLight)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LoadingIndicator(modifier = Modifier.size(28.dp))
        Text(text = text, fontFamily = SemiBoldFont(), fontSize = 15.sp, color = ColorPrimary)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLarge)
            .background(ColorSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = title, fontFamily = BoldFont(), fontSize = 18.sp, color = ColorTextPrimary)
        content()
    }
}

@Composable
private fun PlayersCard(viewData: OnlineRoom.ViewData, room: Room) {
    val accepted = room.accepted.sortedByDescending { it.userId == room.hostId }
    val colors = PawnColor.defaultsFor(accepted.size.coerceAtLeast(2))
    SectionCard(stringResource(Res.string.online_players_title, accepted.size, room.maxPlayers)) {
        repeat(room.maxPlayers) { seat ->
            val member = accepted.getOrNull(seat)
            if (seat > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(ColorDivider))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp),
            ) {
                if (room.game == OnlineGame.LUDO.key) {
                    LudoPawn(
                        color = if (member == null) ColorDivider else colors[seat].color,
                        height = 28.dp,
                    )
                } else {
                    DieFace(value = if (member == null) null else seat + 1, size = 28.dp)
                }
                if (member == null) {
                    Text(
                        text = stringResource(Res.string.online_empty_seat),
                        fontFamily = RegularFont(),
                        fontSize = 15.sp,
                        color = ColorTextSecondary,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Text(
                        text = member.username,
                        fontFamily = SemiBoldFont(),
                        fontSize = 16.sp,
                        color = ColorTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (member.userId == room.hostId) Badge(stringResource(Res.string.online_host_badge))
                    if (member.userId == viewData.myUserId) Badge(stringResource(Res.string.online_you_badge))
                    Spacer(Modifier.weight(1f))
                    if (viewData.isHost && member.userId != room.hostId) {
                        TextButton(
                            onClick = { viewData.listener.onRemoveClick(member.userId) },
                            enabled = viewData.busyUserId == null,
                        ) {
                            Text(
                                text = stringResource(Res.string.online_remove),
                                fontFamily = SemiBoldFont(),
                                color = ColorAccent,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Badge(text: String) {
    Text(
        text = text,
        fontFamily = BoldFont(),
        fontSize = 11.sp,
        color = ColorPrimary,
        modifier = Modifier
            .clip(ShapeSmall)
            .background(ColorPrimaryLight)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun RequestsCard(viewData: OnlineRoom.ViewData, pending: List<RoomMember>) {
    val full = viewData.room?.isFull == true
    SectionCard(stringResource(Res.string.online_requests_title)) {
        pending.forEach { member ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = member.username,
                    fontFamily = SemiBoldFont(),
                    fontSize = 16.sp,
                    color = ColorTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = { viewData.listener.onDeclineClick(member.userId) },
                    enabled = viewData.busyUserId == null,
                ) {
                    Text(stringResource(Res.string.online_decline), fontFamily = SemiBoldFont(), color = ColorAccent)
                }
                LoadingButton(
                    text = stringResource(Res.string.online_accept),
                    onClick = { viewData.listener.onAcceptClick(member.userId) },
                    isLoading = viewData.busyUserId == member.userId,
                    enabled = viewData.busyUserId == null && !full,
                    modifier = Modifier.width(110.dp),
                )
            }
        }
    }
}

@Composable
private fun BottomActions(viewData: OnlineRoom.ViewData) {
    val listener = viewData.listener
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorSurface)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (viewData.isHost) {
            LoadingButton(
                text = stringResource(Res.string.online_start_button),
                onClick = listener::onStartClick,
                isLoading = viewData.isStarting,
                enabled = viewData.canStart,
                backgroundColor = ColorFeltDark,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(Res.string.online_start_hint),
                fontFamily = RegularFont(),
                fontSize = 12.sp,
                color = ColorTextSecondary,
                textAlign = TextAlign.Center,
            )
        } else {
            LoadingButton(
                text = stringResource(
                    if (viewData.isPending) Res.string.online_cancel_request else Res.string.online_leave_room
                ),
                onClick = listener::onLeaveClick,
                isLoading = viewData.isLeaving,
                backgroundColor = ColorPrimaryLight,
                textColor = ColorPrimary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private const val COPIED_MILLIS = 1_500L

private val previewListener = object : OnlineRoomViewListener {
    override fun onAcceptClick(userId: String) = Unit
    override fun onDeclineClick(userId: String) = Unit
    override fun onRemoveClick(userId: String) = Unit
    override fun onStartClick() = Unit
    override fun onLeaveClick() = Unit
}

@Preview
@Composable
private fun OnlineRoomHostPreview() {
    AppTheme {
        OnlineRoomScreen(
            OnlineRoom.ViewData(
                room = Room(
                    id = "1",
                    code = "K7M2QX",
                    game = "ludo",
                    hostId = "a",
                    maxPlayers = 4,
                    status = RoomStatus.OPEN,
                    members = listOf(
                        RoomMember("a", "ana", MemberStatus.ACCEPTED, null),
                        RoomMember("b", "marko", MemberStatus.ACCEPTED, null),
                        RoomMember("c", "jelena", MemberStatus.PENDING, null),
                    ),
                ),
                myUserId = "a",
                listener = previewListener,
            )
        )
    }
}
