package com.cubes.template.shared.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.cubes.template.shared.AppInfo
import com.cubes.template.shared.OpenUrlHandler
import com.cubes.template.shared.data.auth.AuthRepository
import com.cubes.template.shared.data.auth.PlaceholderAuthRepository
import com.cubes.template.shared.data.local.DataStoreRepository
import com.cubes.template.shared.data.local.LocalDataSource
import com.cubes.template.shared.data.network.ConnectivityMonitor
import com.cubes.template.shared.data.remote.ApiClient
import com.cubes.template.shared.data.remote.NoAuthExtension
import com.cubes.template.shared.data.remote.RemoteDataSource
import com.cubes.template.shared.data.session.UserSession
import com.cubes.template.shared.ui.common.popup.GlobalPopupManager
import com.cubes.template.shared.ui.common.popup.PopupManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppCompositionRoot(
    val baseUrl: String,
    private val baseApiUrl: String,
    private val dataStore: DataStore<Preferences>,
    val appInfo: AppInfo,
    val trustAllCertificates: Boolean,
    val connectivityMonitor: ConnectivityMonitor,
) {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val dataStoreRepository by lazy { DataStoreRepository(dataStore) }

    val localDataSource: LocalDataSource by lazy {
        LocalDataSource(dataStoreRepository)
    }

    private val apiClient by lazy {
        ApiClient(
            baseApiUrl = baseApiUrl,
            apiClientAuthExtension = NoAuthExtension(),
            trustAllCertificates = trustAllCertificates,
        )
    }

    val remoteDataSource by lazy {
        RemoteDataSource(apiClient = apiClient)
    }

    val userSession by lazy { UserSession(localDataSource) }

    val authRepository: AuthRepository by lazy { PlaceholderAuthRepository() }

    val popupManager: PopupManager by lazy { GlobalPopupManager() }

    val openUrlHandler by lazy { OpenUrlHandler() }
}
