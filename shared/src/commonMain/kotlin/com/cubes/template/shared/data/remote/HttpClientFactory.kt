package com.cubes.template.shared.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

expect fun createHttpClient(
    trustAllCertificates: Boolean,
    config: HttpClientConfig<*>.() -> Unit,
): HttpClient
