package com.cubes.template.shared.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.app_name
import com.cubes.template.shared.resources.auth_email
import com.cubes.template.shared.resources.auth_forgot_password
import com.cubes.template.shared.resources.auth_hide_password
import com.cubes.template.shared.resources.auth_login_button
import com.cubes.template.shared.resources.auth_online_hint
import com.cubes.template.shared.resources.auth_password
import com.cubes.template.shared.resources.auth_register_button
import com.cubes.template.shared.resources.auth_show_password
import com.cubes.template.shared.resources.auth_skip
import com.cubes.template.shared.resources.auth_skip_hint
import com.cubes.template.shared.resources.auth_tab_login
import com.cubes.template.shared.resources.auth_tab_register
import com.cubes.template.shared.resources.auth_username
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorFeltDark
import com.cubes.template.shared.ui.common.ColorFeltLight
import com.cubes.template.shared.ui.common.ColorPrimary
import com.cubes.template.shared.ui.common.ColorPrimaryLight
import com.cubes.template.shared.ui.common.ColorSurface
import com.cubes.template.shared.ui.common.ColorTextSecondary
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.SemiBoldFont
import com.cubes.template.shared.ui.common.clearFocusOnTapOutside
import com.cubes.template.shared.ui.common.views.AnimatedDie
import com.cubes.template.shared.ui.common.views.DieAnimation
import com.cubes.template.shared.ui.common.views.BackArrow
import com.cubes.template.shared.ui.common.views.buttons.LoadingButton
import org.jetbrains.compose.resources.stringResource

private val ContentMaxWidth = 480.dp

@Composable
fun AuthView(viewModel: AuthViewModel) {
    val viewData by viewModel.viewData.collectAsState()
    AuthScreen(viewData = viewData)
}

@Composable
fun AuthScreen(viewData: Auth.ViewData) {
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
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Logo()
            Spacer(Modifier.height(28.dp))
            AuthCard(viewData, modifier = Modifier.widthIn(max = ContentMaxWidth).fillMaxWidth())
            if (!viewData.canGoBack) {
                Spacer(Modifier.height(20.dp))
                TextButton(onClick = listener::onSkipClick, enabled = !viewData.isLoading) {
                    Text(
                        text = stringResource(Res.string.auth_skip),
                        fontFamily = BoldFont(),
                        fontSize = 17.sp,
                        color = ColorWhite,
                        textDecoration = TextDecoration.Underline,
                    )
                }
                Text(
                    text = stringResource(Res.string.auth_skip_hint),
                    fontFamily = RegularFont(),
                    fontSize = 13.sp,
                    color = ColorWhite.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (viewData.canGoBack) {
            BackArrow(
                onClick = listener::onBackClick,
                modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(start = 4.dp, top = 4.dp),
            )
        }
    }
}

@Composable
private fun Logo() {
    var rollKey by remember { mutableStateOf(0) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { rollKey++ },
        ) {
            repeat(5) { index ->
                AnimatedDie(
                    value = 6,
                    rollKey = rollKey,
                    size = 40.dp,
                    highlighted = true,
                    startDelayMillis = index * DieAnimation.STAGGER_MILLIS,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.app_name),
            fontFamily = ExtraBoldFont(),
            fontSize = 34.sp,
            color = ColorWhite,
        )
    }
}

@Composable
private fun AuthCard(viewData: Auth.ViewData, modifier: Modifier = Modifier) {
    val listener = viewData.listener
    val focusManager = LocalFocusManager.current
    val isRegister = viewData.mode == Auth.Mode.REGISTER
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ColorSurface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ModeSwitch(selected = viewData.mode, onSelect = {
            focusManager.clearFocus()
            listener.onModeSelected(it)
        })

        Text(
            text = stringResource(Res.string.auth_online_hint),
            fontFamily = RegularFont(),
            fontSize = 14.sp,
            color = ColorTextSecondary,
        )

        AnimatedVisibility(
            visible = isRegister,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            AuthField(
                value = viewData.username,
                onValueChange = listener::onUsernameChanged,
                label = stringResource(Res.string.auth_username),
                error = viewData.usernameError,
                enabled = !viewData.isLoading,
            )
        }

        AuthField(
            value = viewData.email,
            onValueChange = listener::onEmailChanged,
            label = stringResource(Res.string.auth_email),
            error = viewData.emailError,
            enabled = !viewData.isLoading,
            keyboardType = KeyboardType.Email,
        )

        AuthField(
            value = viewData.password,
            onValueChange = listener::onPasswordChanged,
            label = stringResource(Res.string.auth_password),
            error = viewData.passwordError,
            enabled = !viewData.isLoading,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            onDone = {
                focusManager.clearFocus()
                listener.onSubmitClick()
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailing = { PasswordVisibilityToggle(passwordVisible) { passwordVisible = !passwordVisible } },
        )

        AnimatedVisibility(
            visible = !isRegister,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.align(Alignment.End),
        ) {
            TextButton(onClick = listener::onForgotPasswordClick, enabled = !viewData.isLoading) {
                Text(
                    text = stringResource(Res.string.auth_forgot_password),
                    fontFamily = SemiBoldFont(),
                    color = ColorPrimary,
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        LoadingButton(
            text = stringResource(if (isRegister) Res.string.auth_register_button else Res.string.auth_login_button),
            onClick = {
                focusManager.clearFocus()
                listener.onSubmitClick()
            },
            isLoading = viewData.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ModeSwitch(selected: Auth.Mode, onSelect: (Auth.Mode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(ColorPrimaryLight)
            .padding(4.dp),
    ) {
        Auth.Mode.entries.forEach { mode ->
            val isSelected = mode == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isSelected) ColorPrimary else ColorPrimaryLight)
                    .clickable { onSelect(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(
                        when (mode) {
                            Auth.Mode.LOGIN -> Res.string.auth_tab_login
                            Auth.Mode.REGISTER -> Res.string.auth_tab_register
                        }
                    ),
                    fontFamily = BoldFont(),
                    fontSize = 15.sp,
                    color = if (isSelected) ColorWhite else ColorPrimary,
                )
            }
        }
    }
}

@Composable
internal fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        visualTransformation = visualTransformation,
        trailingIcon = trailing,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
internal fun PasswordVisibilityToggle(visible: Boolean, onToggle: () -> Unit) {
    TextButton(onClick = onToggle) {
        Text(
            text = stringResource(if (visible) Res.string.auth_hide_password else Res.string.auth_show_password),
            fontFamily = SemiBoldFont(),
            color = ColorPrimary,
        )
    }
}

@Preview
@Composable
private fun AuthScreenPreview() {
    AppTheme {
        AuthScreen(
            viewData = Auth.ViewData(
                mode = Auth.Mode.REGISTER,
                emailError = "Enter a valid email address",
                listener = object : AuthViewListener {
                    override fun onModeSelected(mode: Auth.Mode) = Unit
                    override fun onUsernameChanged(username: String) = Unit
                    override fun onEmailChanged(email: String) = Unit
                    override fun onPasswordChanged(password: String) = Unit
                    override fun onSubmitClick() = Unit
                    override fun onForgotPasswordClick() = Unit
                    override fun onSkipClick() = Unit
                    override fun onBackClick() = Unit
                },
            )
        )
    }
}
