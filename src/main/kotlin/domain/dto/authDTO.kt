package com.example.domain.dto

import kotlinx.serialization.Serializable

@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class LoginResponse(val token: String, val userId: Int, val email: String, val roles: List<String>, val perms: List<String>)
