package com.cubes.template.shared.data.remote

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.svg.SvgDecoder

fun createImageLoader(
    context: PlatformContext,
    trustAllCertificates: Boolean,
): ImageLoader = ImageLoader.Builder(context)
    .crossfade(true)
    .components {
        add(SvgDecoder.Factory())
        add(
            KtorNetworkFetcherFactory(
                httpClient = { createHttpClient(trustAllCertificates) {} }
            )
        )
    }
    .build()
