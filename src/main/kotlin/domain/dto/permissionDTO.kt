package com.example.domain.dto

import kotlinx.serialization.Serializable

@Serializable data class CreatePermissionRequest(val code: String, val description: String? = null)
@Serializable data class PermissionResponse(val permissionId: Int, val code: String, val description: String?)
