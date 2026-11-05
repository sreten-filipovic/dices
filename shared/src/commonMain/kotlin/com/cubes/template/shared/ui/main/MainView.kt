package com.cubes.template.shared.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.cubes.template.shared.di.ScreenCompositionRoot
import com.cubes.template.shared.ui.common.ColorBackground
import com.cubes.template.shared.ui.main.demo_details.DemoDetailsView
import com.cubes.template.shared.ui.main.demo_list.DemoListView

/**
 * Hosts the Main flow back stack. Each `entry` creates its ViewModel through
 * [ScreenCompositionRoot] and is scoped to the back stack entry.
 * Add a Scaffold with bottom navigation here when the app needs tabs.
 */
@Composable
fun MainView(
    mainScreenNavigator: MainScreenNavigator,
    screenCompositionRoot: ScreenCompositionRoot,
) {
    val backStack by mainScreenNavigator.backStack.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { mainScreenNavigator.back() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {

                entry<MainScreen.DemoList> {
                    val viewModel = viewModel {
                        screenCompositionRoot.getDemoListViewModel(mainScreenNavigator)
                    }
                    DemoListView(viewModel = viewModel)
                }

                entry<MainScreen.DemoDetails> { screen ->
                    val viewModel = viewModel {
                        screenCompositionRoot.getDemoDetailsViewModel(
                            item = screen.item,
                            onBack = mainScreenNavigator::back,
                        )
                    }
                    DemoDetailsView(viewModel = viewModel)
                }
            }
        )
    }
}
