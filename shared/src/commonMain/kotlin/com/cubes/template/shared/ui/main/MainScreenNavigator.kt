package com.cubes.template.shared.ui.main

import androidx.lifecycle.ViewModel
import com.cubes.template.shared.OpenUrlHandler
import com.cubes.template.shared.domain.model.DemoItem
import com.cubes.template.shared.ui.app.AppScreenNavigator
import com.cubes.template.shared.ui.main.demo_list.DemoListScreenNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Aggregates per-feature navigator interfaces. ViewModels depend only on the narrow
 * feature interface (e.g. [DemoListScreenNavigator]); this class implements all of them.
 */
interface MainScreenNavigator : DemoListScreenNavigator {

    val backStack: StateFlow<List<MainScreen>>

    fun navigateTo(screen: MainScreen)
    fun back()
}

class MainNavigatorImpl(
    initialBackStack: List<MainScreen>,
    val appScreenNavigator: AppScreenNavigator,
    private val openUrlHandler: OpenUrlHandler,
) : ViewModel(), MainScreenNavigator {

    override val backStack = MutableStateFlow(initialBackStack)

    override fun navigateTo(screen: MainScreen) {
        backStack.update { it + screen }
    }

    override fun back() {
        if (backStack.value.size > 1) {
            backStack.update { it.dropLast(1) }
        }
    }

    // DemoListScreenNavigator
    override fun showDemoDetails(item: DemoItem) = navigateTo(MainScreen.DemoDetails(item))
    override fun openUrl(url: String) = openUrlHandler.openUrl(url)
}
