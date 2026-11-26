package com.cubes.template.shared.ui.main.settings

import androidx.lifecycle.ViewModel
import com.cubes.template.shared.ui.common.ThemeController
import com.cubes.template.shared.ui.common.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel(
    private val themeController: ThemeController,
) : ViewModel(), SettingsViewListener {

    private val _viewData = MutableStateFlow(Settings.ViewData(themeMode = themeController.mode, listener = this))
    val viewData: StateFlow<Settings.ViewData> = _viewData.asStateFlow()

    override fun onThemeModeClick(mode: ThemeMode) {
        themeController.select(mode)
        _viewData.update { it.copy(themeMode = mode) }
    }
}
