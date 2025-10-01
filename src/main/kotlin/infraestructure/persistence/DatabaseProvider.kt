package com.example.infraestructure.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

object DatabaseProvider {
    @Volatile private var instance: Database? = null
    private val log = LoggerFactory.getLogger("DatabaseProvider")

    fun init(
        url: String,
        user: String,
        pass: String,
        driver: String = "com.mysql.cj.jdbc.Driver",
        poolSize: Int = 10
    ): Database {
        // doble-chequeo para inicializar una sola vez
        val existing = instance
        if (existing != null) return existing

        synchronized(this) {
            val again = instance
            if (again != null) return again

            val hikari = HikariConfig().apply {
                jdbcUrl = url
                username = user
                password = pass
                driverClassName = driver
                maximumPoolSize = poolSize
            }
            val db = Database.connect(HikariDataSource(hikari))
            log.info("✅ Conexión a la base de datos establecida correctamente")
            instance = db
            return db
        }
    }

    fun get(): Database =
        instance ?: error("DatabaseProvider aún no fue inicializado. Llamá a init() primero.")
}
