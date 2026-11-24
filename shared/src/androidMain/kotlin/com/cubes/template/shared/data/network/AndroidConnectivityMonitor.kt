package com.cubes.template.shared.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidConnectivityMonitor(context: Context) : ConnectivityMonitor {

    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    private val _isOnline = MutableStateFlow(currentlyOnline())
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                _isOnline.value = capabilities.hasInternet()
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
            }
        })
    }

    private fun currentlyOnline(): Boolean =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.hasInternet() == true

    private fun NetworkCapabilities.hasInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
