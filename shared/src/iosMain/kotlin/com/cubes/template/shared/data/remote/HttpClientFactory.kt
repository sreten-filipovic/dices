package com.cubes.template.shared.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURLAuthenticationMethodServerTrust
import platform.Foundation.NSURLCredential
import platform.Foundation.NSURLSessionAuthChallengePerformDefaultHandling
import platform.Foundation.NSURLSessionAuthChallengeUseCredential
import platform.Foundation.credentialForTrust
import platform.Foundation.serverTrust
import platform.Security.SecTrustCopyExceptions
import platform.Security.SecTrustSetExceptions

@OptIn(ExperimentalForeignApi::class)
actual fun createHttpClient(
    trustAllCertificates: Boolean,
    config: HttpClientConfig<*>.() -> Unit,
): HttpClient {
    return HttpClient(Darwin) {
        config(this)
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
            if (trustAllCertificates) {
                handleChallenge { _, _, challenge, completionHandler ->
                    val space = challenge.protectionSpace
                    val serverTrust = space.serverTrust
                    if (space.authenticationMethod == NSURLAuthenticationMethodServerTrust && serverTrust != null) {
                        val exceptions = SecTrustCopyExceptions(serverTrust)
                        SecTrustSetExceptions(serverTrust, exceptions)
                        completionHandler(
                            NSURLSessionAuthChallengeUseCredential,
                            NSURLCredential.credentialForTrust(serverTrust)
                        )
                    } else {
                        completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
                    }
                }
            }
        }
    }
}
