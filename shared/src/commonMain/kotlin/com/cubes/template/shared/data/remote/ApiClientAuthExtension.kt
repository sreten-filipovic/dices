package com.cubes.template.shared.data.remote

import io.ktor.client.HttpClientConfig
import io.ktor.client.request.HttpRequestBuilder

/**
 * Hook for authentication: install Ktor Auth plugin and/or add headers to every request.
 * Provide a real implementation (bearer tokens, refresh logic) when the project has login.
 */
interface ApiClientAuthExtension {

    fun addAuthExtension(httpClientConfig: HttpClientConfig<*>)

    fun addHeaders(httpRequestBuilder: HttpRequestBuilder)
}

/** Default: no authentication. */
class NoAuthExtension : ApiClientAuthExtension {
    override fun addAuthExtension(httpClientConfig: HttpClientConfig<*>) = Unit
    override fun addHeaders(httpRequestBuilder: HttpRequestBuilder) = Unit
}
