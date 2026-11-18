package com.cubes.template.shared

import kotlinx.coroutines.flow.MutableSharedFlow

class OpenUrlHandler {

    val openUrl = MutableSharedFlow<String>(extraBufferCapacity = 1)

    fun openUrl(url: String) {
        openUrl.tryEmit(url)
    }
}
