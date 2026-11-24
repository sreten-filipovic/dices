package com.cubes.template.shared.ui.common.popup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cubes.template.shared.resources.Res
import com.cubes.template.shared.resources.ok
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.BoldFont
import com.cubes.template.shared.ui.common.ColorBlack
import com.cubes.template.shared.ui.common.ColorFeltDark
import com.cubes.template.shared.ui.common.ColorWhite
import com.cubes.template.shared.ui.common.ExtraBoldFont
import com.cubes.template.shared.ui.common.RegularFont
import com.cubes.template.shared.ui.common.clickableNoRipple
import com.cubes.template.shared.ui.common.views.AnimatedDie
import com.cubes.template.shared.ui.common.views.DiceTable
import com.cubes.template.shared.ui.common.views.buttons.Button
import org.jetbrains.compose.resources.stringResource

@Composable
fun InfoPopup(state: PopupState.Info, onDismiss: () -> Unit) {
    val visibility = remember { MutableTransitionState(false).apply { targetState = true } }
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val close = { visibility.targetState = false }

    LaunchedEffect(visibility.isIdle, visibility.currentState) {
        if (visibility.isIdle && !visibility.currentState) currentOnDismiss()
    }

    Dialog(onDismissRequest = close, properties = popupDialogProperties()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AnimatedVisibility(visibleState = visibility, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ColorBlack.copy(alpha = 0.45f))
                        .clickableNoRipple(close),
                )
            }
            AnimatedVisibility(
                visibleState = visibility,
                modifier = Modifier.safeDrawingPadding().padding(horizontal = 16.dp),
                enter = fadeIn() + scaleIn(initialScale = 0.85f),
                exit = fadeOut() + scaleOut(targetScale = 0.9f),
            ) {
                InfoPopupContent(state = state, onClose = close)
            }
        }
    }
}

@Composable
private fun InfoPopupContent(state: PopupState.Info, onClose: () -> Unit) {
    var rollKey by remember { mutableStateOf(0) }

    DiceTable(modifier = Modifier.fillMaxWidth().clickableNoRipple {}) {
        Spacer(Modifier.height(8.dp))
        AnimatedDie(
            value = 1,
            rollKey = rollKey,
            size = 64.dp,
            animateOnAppear = true,
            onClick = { rollKey++ },
        )
        Spacer(Modifier.height(8.dp))
        if (!state.title.isNullOrEmpty()) {
            Text(
                text = state.title,
                fontFamily = ExtraBoldFont(),
                fontSize = 22.sp,
                color = ColorWhite,
                textAlign = TextAlign.Center,
            )
        }
        if (!state.message.isNullOrEmpty()) {
            Text(
                text = state.message,
                fontFamily = RegularFont(),
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = ColorWhite.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(4.dp))
        Button(
            text = stringResource(Res.string.ok),
            onClick = onClose,
            backgroundColor = ColorWhite,
            textColor = ColorFeltDark,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun InfoPopupContentPreview() {
    AppTheme {
        InfoPopupContent(
            state = PopupState.Info(
                title = "Online multiplayer",
                message = "To play online you need to create an account and be connected to the internet.",
            ),
            onClose = {},
        )
    }
}
