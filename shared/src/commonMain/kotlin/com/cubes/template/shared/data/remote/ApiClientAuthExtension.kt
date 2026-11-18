package com.cubes.template.shared.data.remote

import io.ktor.client.HttpClientConfig
import io.ktor.client.request.HttpRequestBuilder

interface ApiClientAuthExtension {

    fun addAuthExtension(httpClientConfig: HttpClientConfig<*>)

    fun addHeaders(httpRequestBuilder: HttpRequestBuilder)
}

class NoAuthExtension : ApiClientAuthExtension {
    override fun addAuthExtension(httpClientConfig: HttpClientConfig<*>) = Unit
    override fun addHeaders(httpRequestBuilder: HttpRequestBuilder) = Unit
}
