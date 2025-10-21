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
        poolSize: Int = 3
    ): Database {
        instance?.let { return it }
        synchronized(this) {
            instance?.let { return it }

            val cfg = HikariConfig().apply {
                jdbcUrl = url
                username = user
                password = pass
                driverClassName = driver

                // ---- Ahorro / scale-to-zero ----
                maximumPoolSize = poolSize
                minimumIdle = 0
                idleTimeout = 60_000          // 60s sin uso => cierra conexión
                maxLifetime = 120_000         // renueva conexiones seguido
                connectionTimeout = 10_000
                validationTimeout = 5_000

                // MySQL quality-of-life
                addDataSourceProperty("cachePrepStmts", "true")
                addDataSourceProperty("prepStmtCacheSize", "250")
                addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                addDataSourceProperty("useServerPrepStmts", "true")
                addDataSourceProperty("rewriteBatchedStatements", "true")
                addDataSourceProperty("tcpKeepAlive", "false")
                addDataSourceProperty("socketTimeout", "60000")
                addDataSourceProperty("connectTimeout", "10000")
                // Charset explícito
                addDataSourceProperty("useUnicode", "true")
                addDataSourceProperty("characterEncoding", "utf8")
            }

            val ds = HikariDataSource(cfg)
            dataSource = ds
            val db = Database.connect(ds)
            instance = db
            log.info("✅ DB conectada (poolSize=$poolSize, minIdle=0, idleTimeout=60s)")
            return db
        }
    }

    fun shutdown() {
        synchronized(this) {
            try {
                dataSource?.close()
                log.info("🛑 Hikari DataSource cerrado")
            } catch (e: Exception) {
                log.warn("Error cerrando DataSource", e)
            } finally {
                dataSource = null
                instance = null
            }
        }
    }
}
