package com.example

import com.example.domain.entities.BigDecimalSerializer
import com.example.infraestructure.persistence.DatabaseProvider
import com.example.infraestructure.persistence.configureDatabases
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.slf4j.LoggerFactory
import java.math.BigDecimal

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

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

    // ---- Config/ENV ----
    val cfg = environment.config
    val mysqlHost = System.getenv("MYSQLHOST") ?: cfg.propertyOrNull("db.mysql.host")?.getString()
    val mysqlPort = System.getenv("MYSQLPORT") ?: cfg.propertyOrNull("db.mysql.port")?.getString() ?: "3306"
    val mysqlDb   = System.getenv("MYSQLDATABASE") ?: cfg.propertyOrNull("db.mysql.database")?.getString() ?: "pena_socios"
    val mysqlUser = System.getenv("MYSQLUSER") ?: System.getenv("DB_USER") ?: cfg.propertyOrNull("db.mysql.user")?.getString() ?: "root"
    val mysqlPass = System.getenv("MYSQLPASSWORD") ?: System.getenv("DB_PASSWORD") ?: cfg.propertyOrNull("db.mysql.password")?.getString() ?: error("Falta MYSQLPASSWORD/DB_PASSWORD")

    val urlFromMysqlVars = mysqlHost?.let {
        "jdbc:mysql://$it:$mysqlPort/$mysqlDb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true&characterEncoding=UTF-8&useUnicode=true"
    }
    val url = urlFromMysqlVars
        ?: System.getenv("DB_URL")
        ?: cfg.propertyOrNull("db.mysql.url")?.getString()
        ?: "jdbc:mysql://localhost:3306/pena_socios?useSSL=false&serverTimezone=UTC"

    // ---- DB/Hikari (pool chico) ----
    val db = DatabaseProvider.init(
        url    = url,
        user   = mysqlUser,
        pass   = mysqlPass,
        driver = "com.mysql.cj.jdbc.Driver",
        poolSize = 3
    )
    log.info("✅ Pool Hikari inicializado (host=${mysqlHost ?: "fallback"}, db=$mysqlDb)")

    // ---- Hook de apagado (cierro pool) ----
    environment.monitor.subscribe(ApplicationStopping) {
        log.info("Recibido ApplicationStopping → cerrando pool…")
        DatabaseProvider.shutdown()
    }

    // ---- Rutas: health sin tocar DB ----
    routing {
        get("/health") { call.respondText("ok") }
    }

    // ---- Tu pipeline habitual ----
    configureHTTP()
    configureSecurity()
    configureDatabases(db)
    configureSerialization()
    configureRouting()
}
