package com.cubes.template.shared.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

fun Modifier.clickableNoRipple(
    onClick: () -> Unit
): Modifier = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
    ) {
        onClick()
    }
}

@Composable
fun Modifier.onClickHideKeyboard(): Modifier {
    val keyboardController = LocalSoftwareKeyboardController.current

    return clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
    ) {
        keyboardController?.hide()
    }
}
