package com.cubes.template.shared.di

import com.cubes.template.shared.StringProvider
import com.cubes.template.shared.StringProviderImpl
import com.cubes.template.shared.domain.model.DemoItem
import com.cubes.template.shared.domain.use_case.GetDemoItemDetailsUseCase
import com.cubes.template.shared.domain.use_case.GetDemoItemsUseCase
import com.cubes.template.shared.ui.common.Validator
import com.cubes.template.shared.ui.main.demo_details.DemoDetailsViewModel
import com.cubes.template.shared.ui.main.demo_list.DemoListScreenNavigator
import com.cubes.template.shared.ui.main.demo_list.DemoListViewModel

/**
 * Factory for screen ViewModels. One `get<Screen>ViewModel(...)` per screen; the screen's
 * navigator interface and arguments come from the caller (MainView), everything else from here.
 */
class ScreenCompositionRoot(
    private val appCompositionRoot: AppCompositionRoot,
) {

    val remoteDataSource get() = appCompositionRoot.remoteDataSource
    val localDataSource get() = appCompositionRoot.localDataSource
    val popupManager get() = appCompositionRoot.popupManager
    val stringProvider: StringProvider get() = StringProviderImpl()
    val validator get() = Validator(stringProvider)

    fun getDemoListViewModel(screenNavigator: DemoListScreenNavigator) = DemoListViewModel(
        screenNavigator = screenNavigator,
        popupManager = popupManager,
        stringProvider = stringProvider,
        getDemoItemsUseCase = GetDemoItemsUseCase(remoteDataSource),
    )

    fun getDemoDetailsViewModel(item: DemoItem, onBack: () -> Unit) = DemoDetailsViewModel(
        item = item,
        onBack = onBack,
        popupManager = popupManager,
        stringProvider = stringProvider,
        localDataSource = localDataSource,
        getDemoItemDetailsUseCase = GetDemoItemDetailsUseCase(remoteDataSource),
    )
}
