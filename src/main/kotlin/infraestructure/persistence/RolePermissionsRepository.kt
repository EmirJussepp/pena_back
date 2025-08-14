package com.example.infraestructure.persistence



import com.example.domain.contracts.RolePermissionsRepository
import com.example.domain.entities.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class RolePermissionsRepository(private val db: Database): RolePermissionsRepository {
    override fun grant(roleName: String, permissionCode: String): Boolean = transaction(db) {
        val roleId = Roles.slice(Roles.roleId).select { Roles.name eq roleName }.firstOrNull()?.get(Roles.roleId) ?: return@transaction false
        val permId = Permissions.slice(Permissions.permissionId).select { Permissions.code eq permissionCode }.firstOrNull()?.get(Permissions.permissionId) ?: return@transaction false
        val exists = RolePermissions.select { (RolePermissions.roleId eq roleId) and (RolePermissions.permissionId eq permId) }.any()
        if (!exists) {
            RolePermissions.insert {
                it[RolePermissions.roleId] = roleId
                it[RolePermissions.permissionId] = permId
            }
        }
        true
    }

    override fun getPermissionsForRole(roleName: String): List<String> = transaction(db) {
        val roleId = Roles.slice(Roles.roleId).select { Roles.name eq roleName }.firstOrNull()?.get(Roles.roleId) ?: return@transaction emptyList()
        (RolePermissions innerJoin Permissions)
            .slice(Permissions.code)
            .select { RolePermissions.roleId eq roleId }
            .map { it[Permissions.code] }
    }
}