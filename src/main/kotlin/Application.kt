package com.example

import com.example.domain.entities.BigDecimalSerializer
import com.example.infraestructure.persistence.DatabaseProvider
import com.example.infraestructure.persistence.configureDatabases
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
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

    // ---- Config DB: prioridad -> MYSQL* (Railway) → DB_* → application.conf → defaults ----
    val cfg = environment.config

    val mysqlHost = System.getenv("MYSQLHOST")
        ?: cfg.propertyOrNull("db.mysql.host")?.getString()
    val mysqlPort = System.getenv("MYSQLPORT")
        ?: cfg.propertyOrNull("db.mysql.port")?.getString()
        ?: "3306"
    val mysqlDb   = System.getenv("MYSQLDATABASE")
        ?: cfg.propertyOrNull("db.mysql.database")?.getString()
        ?: "pena_socios"
    val mysqlUser = System.getenv("MYSQLUSER")
        ?: System.getenv("DB_USER")
        ?: cfg.propertyOrNull("db.mysql.user")?.getString()
        ?: "root"
    val mysqlPass = System.getenv("MYSQLPASSWORD")
        ?: System.getenv("DB_PASSWORD")
        ?: cfg.propertyOrNull("db.mysql.password")?.getString()
        ?: "UilCNIRSqusmCnTlnjXRzEmOsLQuIlIU"

    // Si Railway nos dio host, armamos el JDBC con eso; si no, probamos DB_URL; si no, default local
    val urlFromMysqlVars = mysqlHost?.let {
        "jdbc:mysql://$it:$mysqlPort/$mysqlDb" +
                "?useSSL=false" +
                "&serverTimezone=UTC" +
                "&allowPublicKeyRetrieval=true" +
                "&characterEncoding=UTF-8" +
                "&useUnicode=true" +
                "&rewriteBatchedStatements=true"
    }

    val url = urlFromMysqlVars
        ?: System.getenv("DB_URL")
        ?: cfg.propertyOrNull("db.mysql.url")?.getString()
        ?: "jdbc:mysql://localhost:3306/pena_socios?useSSL=false&serverTimezone=UTC"

    // --- Tamaño del pool desde ENV (default 5) ---
    val poolMax = (System.getenv("DB_POOL_MAX") ?: "5").toIntOrNull() ?: 5

    // ---- DB singleton ----
    val db = DatabaseProvider.init(
        url  = url,
        user = mysqlUser,
        pass = mysqlPass,
        driver = "com.mysql.cj.jdbc.Driver",
        poolSize = poolMax
    )
    log.info("✅ Pool Hikari inicializado (host=${mysqlHost ?: "fallback"}, db=$mysqlDb, max=$poolMax)")

    // --- Hook: cerrar pool al apagar (deploy/redeploy/sleep) ---
    environment.monitor.subscribe(ApplicationStopping) {
        log.info("Recibido ApplicationStopping → cerrando DataSource")
        com.example.infraestructure.persistence.DatabaseProvider.close()
    }

    // ---- Ktor plugins & app wiring ----
    configureSecurity()
    configureDatabases(db)
    configureSerialization()
    configureHTTP()
    configureRouting()
}
