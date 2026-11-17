package com.cubes.template.shared.ui.app

import com.cubes.template.shared.ui.main.MainScreen

/**
 * Top-level destinations. Each one usually hosts its own nested navigator
 * (e.g. Intro flow, Main flow) with its own back stack.
 */
sealed class AppScreen {

    data class Main(val screen: MainScreen = MainScreen.Home) : AppScreen()

    // Example for a future flow:
    // data class Intro(val screens: List<IntroScreen>) : AppScreen()
}
