package com.cubes.template.shared.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse(val message: String? = null, val status: Int? = null)
