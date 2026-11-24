package com.cubes.template.shared.data.network

import kotlinx.coroutines.flow.StateFlow

/** Reports whether the device currently has an internet connection. */
interface ConnectivityMonitor {
    val isOnline: StateFlow<Boolean>
}
