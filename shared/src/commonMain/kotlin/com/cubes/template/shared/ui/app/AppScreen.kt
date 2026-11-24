package com.cubes.template.shared.ui.app

import com.cubes.template.shared.ui.main.MainScreen

sealed class AppScreen {

    data class Auth(val canGoBack: Boolean = false) : AppScreen()

    data class Password(val mode: com.cubes.template.shared.ui.password.Password.Mode, val email: String = "") :
        AppScreen()

    data class Main(val screen: MainScreen = MainScreen.Home) : AppScreen()
}
