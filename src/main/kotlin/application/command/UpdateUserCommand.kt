package com.example.application.command

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserCommand(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,   // si querés permitir cambio de password
    val roles: List<String>? = null // si viene null, no cambia roles
)