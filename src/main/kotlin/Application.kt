package com.example

import com.example.domain.entities.BigDecimalSerializer
import com.example.infraestructure.persistence.DatabaseProvider
import com.example.infraestructure.persistence.configureDatabases
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.slf4j.LoggerFactory
import java.math.BigDecimal

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val log = LoggerFactory.getLogger("Application")

    // ---- JSON ----
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
                serializersModule = SerializersModule {
                    contextual(BigDecimal::class, BigDecimalSerializer)
                }
            }
        )
    }

    // ---- Config DB: primero application.(yaml|conf), sino ENV, sino defaults ----
    val cfg = environment.config

    val url  = cfg.propertyOrNull("db.mysql.url")?.getString()
        ?: System.getenv("DB_URL")
        ?: "jdbc:mysql://localhost:3306/pena_socios?useSSL=false&serverTimezone=UTC"

    val user = cfg.propertyOrNull("db.mysql.user")?.getString()
        ?: System.getenv("DB_USER") ?: "root"

    val pass = cfg.propertyOrNull("db.mysql.password")?.getString()
        ?: System.getenv("DB_PASSWORD") ?: "18122022"

    val drv  = cfg.propertyOrNull("db.mysql.driver")?.getString()
        ?: "com.mysql.cj.jdbc.Driver"

    // ---- DB singleton ----
    val db = DatabaseProvider.init(url, user, pass, drv)
    log.info("✅ Pool Hikari inicializado (una sola vez)")

    // ---- Ktor plugins & app wiring ----
    configureSecurity()
    configureDatabases(db)   // <- usa el DB ya inicializado (no crea nuevos pools)
    configureSerialization()
    configureRouting()
}
