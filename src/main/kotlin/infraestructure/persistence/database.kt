package com.example.infraestructure.persistence

import com.example.domain.entities.Roles
import com.example.domain.entities.Permissions
import com.example.domain.entities.RolePermissions
import com.example.domain.entities.UserRoles
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils

/** Llamá a esto en el arranque pasando el Database. */
fun configureDatabases(db: Database) {
    // Crea/actualiza solo las tablas RBAC
    transaction(db) {
        SchemaUtils.createMissingTablesAndColumns(
            Roles, Permissions, RolePermissions, UserRoles
        )
    }
    // Seed idempotente
    seedRbac(db)
}

/* -------------------- SEED RBAC (ADMIN / COBRADOR) -------------------- */

private fun seedRbac(db: Database) = transaction(db) {
    fun ensureRole(name: String, desc: String?): Int {
        val existing = Roles.slice(Roles.roleId).select { Roles.name eq name }.firstOrNull()
        return existing?.get(Roles.roleId) ?: (Roles.insert {
            it[Roles.name] = name
            it[Roles.description] = desc
        } get Roles.roleId)
    }

    fun ensurePerm(code: String, desc: String?): Int {
        val existing = Permissions.slice(Permissions.permissionId).select { Permissions.code eq code }.firstOrNull()
        return existing?.get(Permissions.permissionId) ?: (Permissions.insert {
            it[Permissions.code] = code
            it[Permissions.description] = desc
        } get Permissions.permissionId)
    }

    fun grant(roleId: Int, permId: Int) {
        val exists = RolePermissions
            .select { (RolePermissions.roleId eq roleId) and (RolePermissions.permissionId eq permId) }
            .any()
        if (!exists) {
            RolePermissions.insert {
                it[RolePermissions.roleId] = roleId
                it[RolePermissions.permissionId] = permId
            }
        }
    }

    val perms = listOf(
        "app:acceder" to "Puede acceder a la aplicación",
        "usuarios:gestionar" to "CRUD usuarios y roles",
        "socios:ver" to "Ver socios",
        "socios:gestionar" to "Crear/editar/eliminar socios",
        "cuotas:ver" to "Ver cuotas",
        "cuotas:gestionar" to "Crear/editar/eliminar cuotas",
        "beneficios:ver" to "Ver beneficios",
        "beneficios:gestionar" to "Gestionar beneficios",
        "viajes:ver" to "Ver viajes y pasajeros",
        "viajes:gestionar" to "Gestionar viajes y pasajeros",
        "alquileres:ver" to "Ver alquileres",
        "alquileres:gestionar" to "Gestionar alquileres",
        "movimientos:ver" to "Ver listado/detalle de movimientos",
        "movimientos:gestionar" to "Crear/editar/eliminar movimientos"
    )
    val permIds = perms.associate { (code, desc) -> code to ensurePerm(code, desc) }

    val adminId = ensureRole("ADMIN", "Acceso total")
    val cobrId  = ensureRole("COBRADOR", "Acceso cobranzas y viajes")

    // ADMIN: todos
    permIds.values.forEach { grant(adminId, it) }

    // COBRADOR: set acotado
    listOf(
        "app:acceder",
        "socios:ver",
        "cuotas:ver",
        "cuotas:gestionar",
        "beneficios:ver",
        "viajes:gestionar",
        "alquileres:ver"
    ).forEach { code -> grant(cobrId, permIds.getValue(code)) }
}
