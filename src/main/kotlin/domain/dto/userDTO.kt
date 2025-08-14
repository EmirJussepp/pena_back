package com.example.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(val name: String, val email: String, val password: String)
@Serializable
data class UserResponse(val userId: Int, val name: String, val email: String)