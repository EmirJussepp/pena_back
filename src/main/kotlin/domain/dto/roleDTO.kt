package com.example.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateRoleRequest(val name: String, val description: String? = null)
@Serializable
data class RoleResponse(val roleId: Int, val name: String, val description: String?)
