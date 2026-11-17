package com.cubes.template.shared.data.remote

/**
 * Single entry point for all remote calls. Every method returns [Result] with a raw `*Api` DTO.
 * Mapping to domain models happens in use cases.
 *
 * As the API grows, split it into feature interfaces (e.g. `AuthApi`, `ProfileApi`) and
 * delegate to them here: `class RemoteDataSource(...) : AuthApi by authApi`.
 */
class RemoteDataSource(
    @Suppress("unused") private val apiClient: ApiClient,
)
