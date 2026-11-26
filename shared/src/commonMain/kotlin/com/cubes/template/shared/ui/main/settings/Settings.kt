package com.cubes.template.shared.ui.main.settings

import com.cubes.template.shared.ui.common.ThemeMode

object Settings {

    data class ViewData(
        val themeMode: ThemeMode = ThemeMode.LIGHT,
        val listener: SettingsViewListener,
    )
}

interface SettingsViewListener {
    fun onThemeModeClick(mode: ThemeMode)
}
