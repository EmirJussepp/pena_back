package com.example.domain.contracts

interface RolePermissionsRepository {
    fun grant(roleName: String, permissionCode: String): Boolean
    fun getPermissionsForRole(roleName: String): List<String>
}
