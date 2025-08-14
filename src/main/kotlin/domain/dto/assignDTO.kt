package com.example.domain.dto

import kotlinx.serialization.Serializable

@Serializable data class AssignRolesRequest(val roles: List<String>)          // por nombre
@Serializable data class GrantPermissionsRequest(val permissions: List<String>) // por code