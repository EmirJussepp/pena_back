//package com.example.infraestructure.persistence
//
//
//import org.jetbrains.exposed.sql.Database
//import io.ktor.server.application.*
//import org.slf4j.LoggerFactory
//
//fun Application.configureDatabases() {
//    connectToMySql()
//}
//
//fun Application.connectToMySql(): Database? {
//    val log = LoggerFactory.getLogger("Database")
//
//    return try {
//        val config = environment.config
//        val dbUrl = config.propertyOrNull("db.mysql.url")?.getString() ?: error("Database URL not found")
//        val dbUser = config.propertyOrNull("db.mysql.user")?.getString() ?: error("Database User not found")
//        val dbPassword = config.propertyOrNull("db.mysql.password")?.getString() ?: error("Database Password not found")
//        val dbDriver = config.propertyOrNull("db.mysql.driver")?.getString() ?: "com.mysql.cj.jdbc.Driver"
//
//        val database = Database.connect(
//            url = dbUrl,
//            driver = dbDriver,
//            user = dbUser,
//            password = dbPassword
//        )
//
//        log.info("✅ Conexión a la base de datos establecida correctamente")
//        database
//    } catch (e: Exception) {
//        log.error("❌ Error al conectar con la base de datos: ${e.message}")
//        null
//    }
//}


package com.example.infraestructure.persistence

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.jetbrains.exposed.sql.SchemaUtils

// 👉 Importá tus tablas Exposed
import com.example.domain.entities.Users
import com.example.domain.entities.Roles
import com.example.domain.entities.Permissions
import com.example.domain.entities.RolePermissions
import com.example.domain.entities.UserRoles

fun Application.configureDatabases() {
    val db = connectToMySql() ?: return

    // Crear tablas RBAC si faltan (no toca tus otras tablas)
    transaction(db) {
        SchemaUtils.createMissingTablesAndColumns(
            Roles, Permissions, RolePermissions, UserRoles
        )
    }

    // Seed idempotente de roles/permisos
    seedRbac(db)
}

fun Application.connectToMySql(): Database? {
    val log = LoggerFactory.getLogger("Database")

    return try {
        val config = environment.config
        val dbUrl = config.propertyOrNull("db.mysql.url")?.getString() ?: error("Database URL not found")
        val dbUser = config.propertyOrNull("db.mysql.user")?.getString() ?: error("Database User not found")
        val dbPassword = config.propertyOrNull("db.mysql.password")?.getString() ?: error("Database Password not found")
        val dbDriver = config.propertyOrNull("db.mysql.driver")?.getString() ?: "com.mysql.cj.jdbc.Driver"

        val database = Database.connect(
            url = dbUrl,
            driver = dbDriver,
            user = dbUser,
            password = dbPassword
        )

        log.info("✅ Conexión a la base de datos establecida correctamente")
        database
    } catch (e: Exception) {
        log.error("❌ Error al conectar con la base de datos: ${e.message}")
        null
    }
}

/* -------------------- SEED RBAC (ADMIN / COBRADOR) -------------------- */

private fun seedRbac(db: Database) = transaction(db) {
    // Helpers idempotentes
    fun ensureRole(name: String, desc: String?): Int {
        val existing = Roles
            .slice(Roles.roleId)
            .select { Roles.name eq name }
            .firstOrNull()
        return existing?.get(Roles.roleId) ?: (Roles.insert {
            it[Roles.name] = name
            it[Roles.description] = desc
        } get Roles.roleId)
    }

    fun ensurePerm(code: String, desc: String?): Int {
        val existing = Permissions
            .slice(Permissions.permissionId)
            .select { Permissions.code eq code }
            .firstOrNull()
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

    // ---- Permisos base (los que definimos juntos) ----
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

    val permIds: Map<String, Int> = perms.associate { (code, desc) -> code to ensurePerm(code, desc) }

    // ---- Roles ----
    val adminId = ensureRole("ADMIN", "Acceso total")
    val cobrId  = ensureRole("COBRADOR", "Acceso cobranzas y viajes")

    // ADMIN: todos los permisos
    permIds.values.forEach { grant(adminId, it) }

    // COBRADOR: según lo pedido (SIN movimientos, alquileres solo ver, viajes full)
    listOf(
        "app:acceder",
        "socios:ver",
        "cuotas:ver",
        "cuotas:gestionar", // si querés que no gestione, cambiá por solo 'cuotas:ver'
        "beneficios:ver",
        "viajes:gestionar",
        "alquileres:ver"
    ).forEach { code -> grant(cobrId, permIds.getValue(code)) }
}

