package com.cubes.template

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.cubes.template.shared.AndroidAppInfo
import com.cubes.template.shared.data.local.DataStoreFactoryImpl
import com.cubes.template.shared.data.network.AndroidConnectivityMonitor
import com.cubes.template.shared.di.AppCompositionRoot

class MainApplication : Application() {

    companion object {
        const val BASE_URL = BuildConfig.BaseUrl
    }

    private val dataStore: DataStore<Preferences> by lazy { DataStoreFactoryImpl(this).get() }

    val appCompositionRoot: AppCompositionRoot by lazy {
        AppCompositionRoot(
            baseUrl = BASE_URL,
            baseApiUrl = BASE_URL,
            dataStore = dataStore,
            appInfo = AndroidAppInfo(this),
            trustAllCertificates = BuildConfig.BUILD_TYPE != "release",
            connectivityMonitor = AndroidConnectivityMonitor(this),
        )
    }
}
