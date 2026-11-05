package com.cubes.template.shared.ui.common.popup

import androidx.compose.ui.window.DialogProperties

actual fun popupDialogProperties(): DialogProperties = DialogProperties(
    usePlatformDefaultWidth = false,
    usePlatformInsets = false,
)
