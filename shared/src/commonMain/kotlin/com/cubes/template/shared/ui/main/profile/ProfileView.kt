package com.cubes.template.shared.ui.main.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.data.auth.User
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.profile_delete
import com.cubes.template.shared.resources.profile_guest_message
import com.cubes.template.shared.resources.profile_guest_title
import com.cubes.template.shared.resources.profile_login
import com.cubes.template.shared.resources.profile_change_password
import com.cubes.template.shared.resources.profile_logout
import com.cubes.template.shared.resources.profile_save
import com.cubes.template.shared.resources.profile_title
import com.cubes.template.shared.resources.profile_username
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorAccent
import com.cubes.template.shared.ui.common.ColorBackground
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
import com.cubes.template.shared.ui.common.ShapeLarge
import com.cubes.template.shared.ui.common.clearFocusOnTapOutside
import com.cubes.template.shared.ui.common.views.PersonIcon
import com.cubes.template.shared.ui.common.views.Toolbar
import com.cubes.template.shared.ui.common.views.buttons.Button
import com.cubes.template.shared.ui.common.views.buttons.LoadingButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileView(viewModel: ProfileViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    ProfileScreen(viewData = viewData)
}

@Composable
fun ProfileScreen(viewData: Profile.ViewData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
            .imePadding()
            .clearFocusOnTapOutside(),
    ) {
        Toolbar(title = stringResource(Res.string.profile_title))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val user = viewData.user
            if (user == null) GuestContent(viewData) else UserContent(viewData, user)
        }
    }
}

@Composable
private fun Avatar(initial: String?) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(ColorFeltLight, ColorFeltDark))),
        contentAlignment = Alignment.Center,
    ) {
        if (initial == null) {
            PersonIcon(color = ColorWhite, size = 48.dp)
        } else {
            Text(text = initial, fontFamily = ExtraBoldFont(), fontSize = 38.sp, color = ColorWhite)
        }
    }
}

@Composable
private fun GuestContent(viewData: Profile.ViewData) {
    Spacer(Modifier.height(24.dp))
    Avatar(initial = null)
    Text(
        text = stringResource(Res.string.profile_guest_title),
        fontFamily = BoldFont(),
        fontSize = 22.sp,
        color = ColorTextPrimary,
    )
    Text(
        text = stringResource(Res.string.profile_guest_message),
        fontFamily = RegularFont(),
        fontSize = 15.sp,
        color = ColorTextSecondary,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(8.dp))
    Button(
        text = stringResource(Res.string.profile_login),
        onClick = viewData.listener::onLoginClick,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun UserContent(viewData: Profile.ViewData, user: User) {
    val listener = viewData.listener
    val focusManager = LocalFocusManager.current

    Avatar(initial = user.username.firstOrNull()?.uppercase())
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = user.username,
            fontFamily = BoldFont(),
            fontSize = 22.sp,
            color = ColorTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = user.email,
            fontFamily = RegularFont(),
            fontSize = 14.sp,
            color = ColorTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLarge)
            .background(ColorSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = viewData.usernameInput,
            onValueChange = listener::onUsernameChanged,
            label = { Text(stringResource(Res.string.profile_username)) },
            isError = viewData.usernameError != null,
            supportingText = viewData.usernameError?.let { { Text(it) } },
            singleLine = true,
            enabled = !viewData.isBusy,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                listener.onSaveUsernameClick()
            }),
            modifier = Modifier.fillMaxWidth(),
        )
        LoadingButton(
            text = stringResource(Res.string.profile_save),
            onClick = {
                focusManager.clearFocus()
                listener.onSaveUsernameClick()
            },
            isLoading = viewData.isSavingUsername,
            enabled = viewData.canSaveUsername,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Spacer(Modifier.height(8.dp))
    Button(
        text = stringResource(Res.string.profile_change_password),
        onClick = listener::onChangePasswordClick,
        enabled = !viewData.isBusy,
        backgroundColor = ColorPrimaryLight,
        textColor = ColorPrimary,
        modifier = Modifier.fillMaxWidth(),
    )
    Button(
        text = stringResource(Res.string.profile_logout),
        onClick = listener::onLogoutClick,
        enabled = !viewData.isBusy,
        backgroundColor = ColorPrimaryLight,
        textColor = ColorPrimary,
        modifier = Modifier.fillMaxWidth(),
    )
    LoadingButton(
        text = stringResource(Res.string.profile_delete),
        onClick = listener::onDeleteAccountClick,
        isLoading = viewData.isDeleting,
        enabled = !viewData.isBusy,
        backgroundColor = ColorSurface,
        textColor = ColorAccent,
        modifier = Modifier.fillMaxWidth(),
    )
}

private val previewListener = object : ProfileViewListener {
    override fun onLoginClick() = Unit
    override fun onUsernameChanged(username: String) = Unit
    override fun onSaveUsernameClick() = Unit
    override fun onChangePasswordClick() = Unit
    override fun onLogoutClick() = Unit
    override fun onDeleteAccountClick() = Unit
}

@Preview
@Composable
private fun ProfileGuestPreview() {
    AppTheme { ProfileScreen(Profile.ViewData(listener = previewListener)) }
}

@Preview
@Composable
private fun ProfileUserPreview() {
    AppTheme {
        ProfileScreen(
            Profile.ViewData(
                user = User(id = "preview", username = "ana", email = "ana@example.com"),
                usernameInput = "ana_k",
                listener = previewListener,
            )
        )
    }
}
