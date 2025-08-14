package com.example.domain.entities

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption

object Roles : Table("roles") {
    val roleId = integer("role_id").autoIncrement()
    val name = varchar("name", 50).uniqueIndex()
    val description = varchar("description", 255).nullable()
    override val primaryKey = PrimaryKey(roleId)
}

object Permissions : Table("permissions") {
    val permissionId = integer("permission_id").autoIncrement()
    val code = varchar("code", 100).uniqueIndex() // ej: "socios:ver"
    val description = varchar("description", 255).nullable()
    override val primaryKey = PrimaryKey(permissionId)
}

object RolePermissions : Table("role_permissions") {
    val roleId = integer("role_id").references(Roles.roleId, onDelete = ReferenceOption.CASCADE)
    val permissionId = integer("permission_id").references(Permissions.permissionId, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(roleId, permissionId)
}

object UserRoles : Table("user_roles") {
    val userId = integer("user_id").references(Users.userId, onDelete = ReferenceOption.CASCADE)
    val roleId = integer("role_id").references(Roles.roleId, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(userId, roleId)
}