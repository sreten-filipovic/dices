package com.cubes.template.shared.data.remote.response

import kotlinx.serialization.Serializable

/**
 * DTO for the demo endpoint (JSONPlaceholder /posts).
 * Replace with the real API models; keep the `*Api` suffix convention.
 */
@Serializable
data class DemoItemApi(
    val id: Int,
    val userId: Int? = null,
    val title: String? = null,
    val body: String? = null,
)
