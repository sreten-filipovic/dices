package com.cubes.template.shared.data.remote

import kotlinx.serialization.Serializable

/**
 * Minimal error envelope the backend returns on 4xx responses.
 * Adjust the fields to match the real API contract.
 */
@Serializable
data class BaseResponse(val message: String? = null, val status: Int? = null)
