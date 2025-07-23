package com.example.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)