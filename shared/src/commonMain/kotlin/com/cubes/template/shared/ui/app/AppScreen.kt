package com.cubes.template.shared.ui.app

import com.cubes.template.shared.ui.main.MainScreen

sealed class AppScreen {

    data class Main(val screen: MainScreen = MainScreen.Home) : AppScreen()
}
