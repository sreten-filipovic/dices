package com.cubes.template.shared.data.remote

import com.cubes.template.shared.log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Thin wrapper around Ktor that maps every call to [Result] and
 * translates known backend error messages into [KnownApiException].
 */
class ApiClient(
    private val baseApiUrl: String,
    private val apiClientAuthExtension: ApiClientAuthExtension,
    private val trustAllCertificates: Boolean,
    private val enableLogging: Boolean = trustAllCertificates,
) {

    val httpClient: HttpClient by lazy {
        createHttpClient(trustAllCertificates) {
            expectSuccess = true

            defaultRequest {
                url(baseApiUrl)
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }

            if (enableLogging) {
                install(Logging) {
                    level = LogLevel.INFO
                }
            }

            HttpResponseValidator {
                handleResponseExceptionWithRequest { exception, _ ->
                    val clientException = exception as? ClientRequestException
                        ?: return@handleResponseExceptionWithRequest
                    val response = clientException.response
                    log("ApiClient", "Error response body: ${response.bodyAsText()}")

                    val message = try {
                        response.body<BaseResponse>().message
                    } catch (ex: Exception) {
                        null
                    }

                    ApiErrorMessage.find(message)?.let { msg ->
                        throw KnownApiException(msg, exception)
                    }
                }
            }

            apiClientAuthExtension.addAuthExtension(this)
        }
    }

    fun HttpRequestBuilder.addHeaders() {
        apiClientAuthExtension.addHeaders(this)
    }

    suspend inline fun <reified T : Any> get(
        path: String,
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): Result<T> = try {
        val body = httpClient.get(path) {
            addHeaders()
            block()
        }.body<T>()
        Result.Success(body)
    } catch (ex: Exception) {
        log("ApiClient", "GET $path failed: $ex")
        Result.Error(ex)
    }

    suspend inline fun <reified T : Any> post(
        path: String,
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): Result<T> = try {
        val body = httpClient.post(path) {
            addHeaders()
            block()
        }.body<T>()
        Result.Success(body)
    } catch (ex: Exception) {
        log("ApiClient", "POST $path failed: $ex")
        Result.Error(ex)
    }

    fun clearAuthTokens() {
        httpClient.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
