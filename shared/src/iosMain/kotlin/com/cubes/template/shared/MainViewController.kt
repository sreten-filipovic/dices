package com.cubes.template.shared

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.cubes.template.shared.di.AppCompositionRoot
import com.cubes.template.shared.ui.app.App

fun MainViewController(
    appCompositionRoot: AppCompositionRoot,
) = ComposeUIViewController {
    CompositionLocalProvider(LocalOverscrollFactory provides null) {
        App(appCompositionRoot = appCompositionRoot)
    }
}
