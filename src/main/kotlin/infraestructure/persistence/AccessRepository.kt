package com.example.infraestructure.persistence


import com.example.domain.entities.Users
import com.example.domain.entities.Roles
import com.example.domain.entities.RolePermissions
import com.example.domain.entities.UserRoles
import com.example.domain.entities.Permissions
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

data class UserAccess(
    val userId: Int,
    val email: String,
    val roles: List<String>,
    val permissions: List<String>
)

class AccessRepository(private val db: Database) {

    fun getAccessByEmail(email: String): UserAccess? = transaction(db) {
        val u = Users.slice(Users.userId, Users.email)
            .select { Users.email eq email }
            .firstOrNull() ?: return@transaction null

        val uid = u[Users.userId]

        // Roles del usuario
        val roles = UserRoles
            .join(Roles, JoinType.INNER, UserRoles.roleId, Roles.roleId)
            .slice(Roles.name)
            .select { UserRoles.userId eq uid }
            .map { it[Roles.name] }
            .distinct()

        // Permisos del usuario  (user_roles -> role_permissions -> permissions)
        val perms = UserRoles
            .join(RolePermissions, JoinType.INNER, UserRoles.roleId, RolePermissions.roleId)
            .join(Permissions, JoinType.INNER, RolePermissions.permissionId, Permissions.permissionId)
            .slice(Permissions.code)
            .select { UserRoles.userId eq uid }
            .map { it[Permissions.code] }
            .distinct()

        UserAccess(uid, u[Users.email], roles, perms)
    }
}
