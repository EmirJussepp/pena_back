package com.example.infraestructure.persistence


import org.jetbrains.exposed.sql.Database
import io.ktor.server.application.*
import org.slf4j.LoggerFactory

fun Application.configureDatabases() {
    connectToMySql()
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



