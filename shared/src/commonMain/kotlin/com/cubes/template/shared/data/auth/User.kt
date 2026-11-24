package com.cubes.template.shared.data.auth

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val ownedDice: List<String> = emptyList(),
    val ownedPawns: List<String> = emptyList(),
)
