package com.cubes.template.shared.ui.app

import com.cubes.template.shared.ui.main.MainScreen

sealed class AppScreen {

    /** [canGoBack] is true when opened over the app (e.g. from Profile) rather than as the start screen. */
    data class Auth(val canGoBack: Boolean = false) : AppScreen()

    data class Main(val screen: MainScreen = MainScreen.Home) : AppScreen()
}
