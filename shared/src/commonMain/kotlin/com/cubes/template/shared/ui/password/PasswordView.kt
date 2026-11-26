package com.cubes.template.shared.ui.password

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.auth_email
import com.cubes.template.shared.resources.password_change_hint
import com.cubes.template.shared.resources.password_change_title
import com.cubes.template.shared.resources.password_code
import com.cubes.template.shared.resources.password_code_hint
import com.cubes.template.shared.resources.password_confirm
import com.cubes.template.shared.resources.password_current
import com.cubes.template.shared.resources.password_email_hint
import com.cubes.template.shared.resources.password_forgot_title
import com.cubes.template.shared.resources.password_new
import com.cubes.template.shared.resources.password_resend_code
import com.cubes.template.shared.resources.password_save
import com.cubes.template.shared.resources.password_send_code
import com.cubes.template.shared.ui.auth.AuthField
import com.cubes.template.shared.ui.auth.PasswordVisibilityToggle
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.ColorFeltDark
import com.cubes.template.shared.ui.common.ColorFeltLight
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorSurface
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.SemiBoldFont
import com.cubes.template.shared.ui.common.ShapeLarge
import com.cubes.template.shared.ui.common.clearFocusOnTapOutside
import com.cubes.template.shared.ui.common.views.BackArrow
import com.cubes.template.shared.ui.common.views.buttons.LoadingButton
import org.jetbrains.compose.resources.stringResource

private val ContentMaxWidth = 480.dp

@Composable
fun PasswordView(viewModel: PasswordViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    PasswordScreen(viewData = viewData)
}

@Composable
fun PasswordScreen(viewData: Password.ViewData) {
    val listener = viewData.listener
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(ColorFeltLight, ColorFeltDark)))
            .clearFocusOnTapOutside(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(
                    if (viewData.mode == Password.Mode.CHANGE) Res.string.password_change_title
                    else Res.string.password_forgot_title
                ),
                fontFamily = ExtraBoldFont(),
                fontSize = 30.sp,
                color = ColorWhite,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            AnimatedContent(
                targetState = viewData.step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth(),
            ) { step ->
                PasswordCard(viewData.copy(step = step))
            }
        }

        BackArrow(
            onClick = listener::onBackClick,
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(start = 4.dp, top = 4.dp),
        )
    }
}

@Composable
private fun PasswordCard(viewData: Password.ViewData) {
    val listener = viewData.listener
    val focusManager = LocalFocusManager.current
    var passwordsVisible by remember { mutableStateOf(false) }
    val transformation = if (passwordsVisible) VisualTransformation.None else PasswordVisualTransformation()
    val toggle: @Composable () -> Unit = {
        PasswordVisibilityToggle(passwordsVisible) { passwordsVisible = !passwordsVisible }
    }
    val submit = {
        focusManager.clearFocus()
        listener.onSubmitClick()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ShapeLarge)
            .background(ColorSurface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = when {
                viewData.showsCurrentPassword -> stringResource(Res.string.password_change_hint)
                viewData.showsEmail -> stringResource(Res.string.password_email_hint)
                else -> stringResource(Res.string.password_code_hint, viewData.email.trim())
            },
            fontFamily = RegularFont(),
            fontSize = 14.sp,
            color = ColorTextSecondary,
        )

        if (viewData.showsEmail) {
            AuthField(
                value = viewData.email,
                onValueChange = listener::onEmailChanged,
                label = stringResource(Res.string.auth_email),
                error = viewData.emailError,
                enabled = !viewData.isLoading,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                onDone = submit,
            )
        }

        if (viewData.showsCode) {
            AuthField(
                value = viewData.code,
                onValueChange = listener::onCodeChanged,
                label = stringResource(Res.string.password_code),
                error = viewData.codeError,
                enabled = !viewData.isLoading && !viewData.isCodeVerified,
                keyboardType = KeyboardType.NumberPassword,
            )
        }

        if (viewData.showsCurrentPassword) {
            AuthField(
                value = viewData.currentPassword,
                onValueChange = listener::onCurrentPasswordChanged,
                label = stringResource(Res.string.password_current),
                error = viewData.currentPasswordError,
                enabled = !viewData.isLoading,
                keyboardType = KeyboardType.Password,
                visualTransformation = transformation,
                trailing = toggle,
            )
        }

        if (viewData.showsNewPassword) {
            AuthField(
                value = viewData.newPassword,
                onValueChange = listener::onNewPasswordChanged,
                label = stringResource(Res.string.password_new),
                error = viewData.newPasswordError,
                enabled = !viewData.isLoading,
                keyboardType = KeyboardType.Password,
                visualTransformation = transformation,
                trailing = toggle,
            )
            AuthField(
                value = viewData.confirmPassword,
                onValueChange = listener::onConfirmPasswordChanged,
                label = stringResource(Res.string.password_confirm),
                error = viewData.confirmPasswordError,
                enabled = !viewData.isLoading,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onDone = submit,
                visualTransformation = transformation,
                trailing = toggle,
            )
        }

        Spacer(Modifier.height(4.dp))
        LoadingButton(
            text = stringResource(if (viewData.showsEmail) Res.string.password_send_code else Res.string.password_save),
            onClick = submit,
            isLoading = viewData.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        if (viewData.showsCode && !viewData.isCodeVerified) {
            TextButton(
                onClick = listener::onResendCodeClick,
                enabled = !viewData.isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(
                    text = stringResource(Res.string.password_resend_code),
                    fontFamily = SemiBoldFont(),
                    color = ColorPrimary,
                )
            }
        }
    }
}

private val previewListener = object : PasswordViewListener {
    override fun onEmailChanged(email: String) = Unit
    override fun onCodeChanged(code: String) = Unit
    override fun onCurrentPasswordChanged(password: String) = Unit
    override fun onNewPasswordChanged(password: String) = Unit
    override fun onConfirmPasswordChanged(password: String) = Unit
    override fun onSubmitClick() = Unit
    override fun onResendCodeClick() = Unit
    override fun onBackClick() = Unit
}

@Preview
@Composable
private fun ChangePasswordPreview() {
    AppTheme {
        PasswordScreen(Password.ViewData(Password.Mode.CHANGE, Password.Step.NEW_PASSWORD, listener = previewListener))
    }
}

@Preview
@Composable
private fun ResetPasswordCodePreview() {
    AppTheme {
        PasswordScreen(
            Password.ViewData(
                mode = Password.Mode.FORGOT,
                step = Password.Step.CODE,
                email = "ana@example.com",
                listener = previewListener,
            )
        )
    }
}
