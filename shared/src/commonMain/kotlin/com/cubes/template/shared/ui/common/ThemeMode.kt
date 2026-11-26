package com.cubes.template.shared.ui.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cubes.template.shared.data.local.LocalDataSource

enum class ThemeMode { LIGHT, DARK }

object ThemeState {
    var mode by mutableStateOf(ThemeMode.LIGHT)
    val isDark: Boolean get() = mode == ThemeMode.DARK
}

class ThemeController(private val localDataSource: LocalDataSource) {

    init {
        ThemeState.mode = localDataSource.getThemeMode()
            ?.let { saved -> ThemeMode.entries.firstOrNull { it.name == saved } }
            ?: ThemeMode.LIGHT
    }

    val mode: ThemeMode get() = ThemeState.mode

    fun select(mode: ThemeMode) {
        if (ThemeState.mode == mode) return
        ThemeState.mode = mode
        localDataSource.saveThemeMode(mode.name)
    }
}
