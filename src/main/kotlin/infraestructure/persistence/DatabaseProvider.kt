package com.example.infraestructure.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

object DatabaseProvider {
    @Volatile private var instance: Database? = null
    @Volatile private var dataSource: HikariDataSource? = null
    private val log = LoggerFactory.getLogger("DatabaseProvider")

    fun init(
        url: String,
        user: String,
        pass: String,
        driver: String = "com.mysql.cj.jdbc.Driver",
        poolSize: Int = 10 // ajustado a algo chico
    ): Database {
        instance?.let { return it }

        synchronized(this) {
            instance?.let { return it }

            val hikariCfg = HikariConfig().apply {
                jdbcUrl = url
                username = user
                password = pass
                driverClassName = driver

                // --- Sleep-friendly ---
                maximumPoolSize = poolSize
                minimumIdle = 0                   // no mantengas conexiones ociosas
                idleTimeout = 120_000              // 60s para cerrar ociosas (podés subir a 120_000)
                keepaliveTime = 0                 // evita heartbeats
                maxLifetime = 15 * 60_000         // recambio prudente (15 min)
                connectionTimeout = 10_000        // espera razonable al pedir conexión
                // Opcional para debug:
                // leakDetectionThreshold = 10_000
            }

            val ds = HikariDataSource(hikariCfg)
            val db = Database.connect(ds)

            dataSource = ds
            instance = db
            log.info("✅ Conexión a la base de datos establecida correctamente (sleep-friendly)")
            return db
        }
    }

    fun close() {
        synchronized(this) {
            runCatching { dataSource?.close() }
                .onSuccess { log.info("🛑 DataSource cerrado correctamente") }
                .onFailure { log.warn("Error cerrando DataSource", it) }
            dataSource = null
            instance = null
        }
    }

    fun get(): Database =
        instance ?: error("DatabaseProvider aún no fue inicializado. Llamá a init() primero.")
}
