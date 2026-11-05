package com.cubes.template.shared.ui.main

import com.cubes.template.shared.domain.model.DemoItem

/**
 * Destinations inside the Main flow. Group them per feature as the app grows:
 * `sealed class Profile : MainScreen() { data object Home : Profile() ... }`
 */
sealed class MainScreen {
    data object DemoList : MainScreen()
    data class DemoDetails(val item: DemoItem) : MainScreen()
}
