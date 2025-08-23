package com.example.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioConRoles(
    val userId: Int,
    val name: String,
    val email: String,
    val password: String,
    val roles: List<Role> // o List<String> si vas a usar nombres
)
@Serializable

data class UserResponse(
    val userId: Int,
    val name: String,
    val email: String,
    val roles: List<String> // nombres de los roles para mostrar en frontend
)
//data class UserResponse(val userId: Int, val name: String, val email: String)