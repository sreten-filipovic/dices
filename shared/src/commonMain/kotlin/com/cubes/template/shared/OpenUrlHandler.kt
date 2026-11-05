package com.cubes.template.shared

import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Bridge that lets non-Compose code (ViewModels, navigators) request opening a URL.
 * [com.cubes.template.shared.ui.app.OpenUrlListener] collects the flow and opens it with the platform handler.
 */
class OpenUrlHandler {

    val openUrl = MutableSharedFlow<String>(extraBufferCapacity = 1)

    fun openUrl(url: String) {
        openUrl.tryEmit(url)
    }
}
