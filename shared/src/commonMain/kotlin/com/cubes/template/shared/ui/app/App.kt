package com.cubes.template.shared.ui.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import coil3.compose.setSingletonImageLoaderFactory
import com.cubes.template.shared.OpenUrlHandler
import com.cubes.template.shared.data.remote.createImageLoader
import com.cubes.template.shared.di.AppCompositionRoot
import com.cubes.template.shared.di.ScreenCompositionRoot
import com.cubes.template.shared.ui.auth.AuthView
import com.cubes.template.shared.ui.common.AppTheme
import com.cubes.template.shared.ui.common.LocalTrustAllCertificates
import com.cubes.template.shared.ui.common.popup.GlobalPopupRenderer
import com.cubes.template.shared.ui.main.MainNavigatorImpl
import com.cubes.template.shared.ui.main.MainView
import com.cubes.template.shared.ui.splash.SplashView
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun App(appCompositionRoot: AppCompositionRoot) {

    setSingletonImageLoaderFactory { context ->
        createImageLoader(context, appCompositionRoot.trustAllCertificates)
    }

    val openUrlHandler = appCompositionRoot.openUrlHandler
    val popupManager = appCompositionRoot.popupManager
    val screenCompositionRoot = remember { ScreenCompositionRoot(appCompositionRoot) }

    val startupViewModel: AppViewModel = viewModel {
        AppViewModel(
            localDataSource = appCompositionRoot.localDataSource,
            userSession = appCompositionRoot.userSession,
            authRepository = appCompositionRoot.authRepository,
        )
    }
    val startupState by startupViewModel.state.collectAsState()

    CompositionLocalProvider(LocalTrustAllCertificates provides appCompositionRoot.trustAllCertificates) {
        AppTheme {
            when (val state = startupState) {

                AppStartupState.Loading -> SplashView(
                    showRefresh = false,
                    onRetry = { startupViewModel.retry() }
                )

                AppStartupState.Error -> SplashView(
                    showRefresh = true,
                    onRetry = { startupViewModel.retry() }
                )

                is AppStartupState.Ready -> {
                    val appScreenNavigator = state.navigator
                    val backStack by appScreenNavigator.backStack.collectAsState()
                    val popupState by popupManager.popupState.collectAsState()

                    Box(modifier = Modifier.fillMaxSize()) {
                        NavDisplay(
                            backStack = backStack,
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator()
                            ),
                            onBack = { appScreenNavigator.back() },
                            entryProvider = entryProvider {

                                entry<AppScreen.Auth> { screen ->
                                    val viewModel = viewModel {
                                        screenCompositionRoot.getAuthViewModel(
                                            canGoBack = screen.canGoBack,
                                            onFinished = if (screen.canGoBack) {
                                                appScreenNavigator::back
                                            } else {
                                                appScreenNavigator::showMain
                                            },
                                        )
                                    }
                                    AuthView(viewModel = viewModel)
                                }

                                entry<AppScreen.Main> { entry ->
                                    val mainScreenNavigator = viewModel {
                                        MainNavigatorImpl(
                                            initialBackStack = listOf(entry.screen),
                                            appScreenNavigator = appScreenNavigator,
                                            openUrlHandler = openUrlHandler,
                                        )
                                    }

                                    MainView(
                                        mainScreenNavigator = mainScreenNavigator,
                                        screenCompositionRoot = screenCompositionRoot,
                                    )
                                }
                            }
                        )

                        popupState?.let { currentPopup ->
                            GlobalPopupRenderer(
                                state = currentPopup,
                                onDismiss = { popupManager.dismiss() }
                            )
                        }
                    }

                    OpenUrlListener(openUrlHandler)
                }
            }
        }
    }
}

@Composable
fun OpenUrlListener(openUrlHandler: OpenUrlHandler) {
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(openUrlHandler) {
        openUrlHandler.openUrl
            .onEach { uriHandler.openUri(it) }
            .launchIn(this)
    }
}
