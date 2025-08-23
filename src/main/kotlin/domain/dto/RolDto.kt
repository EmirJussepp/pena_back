package com.example.domain.dto



import kotlinx.serialization.Serializable

@Serializable
data class Role(
    val roleId: Int,
    val name: String,
    val description: String? = null
)
