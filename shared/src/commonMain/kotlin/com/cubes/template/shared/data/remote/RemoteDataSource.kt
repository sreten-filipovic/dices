package com.cubes.template.shared.data.remote

import com.cubes.template.shared.data.remote.response.DemoItemApi

/**
 * Single entry point for all remote calls. Every method returns [Result] with a raw `*Api` DTO.
 * Mapping to domain models happens in use cases.
 *
 * As the API grows, split it into feature interfaces (e.g. `AuthApi`, `ProfileApi`) and
 * delegate to them here: `class RemoteDataSource(...) : AuthApi by authApi`.
 */
class RemoteDataSource(
    private val apiClient: ApiClient,
) {

    suspend fun getDemoItems(): Result<List<DemoItemApi>> =
        apiClient.get("posts")

    suspend fun getDemoItem(id: Int): Result<DemoItemApi> =
        apiClient.get("posts/$id")
}
