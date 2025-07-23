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



//object Users : Table() { // Solo para verificar la conexión
//    val id = integer("id").autoIncrement()
//    val name = varchar("name", 50)
//    override val primaryKey = PrimaryKey(id)
//}
//
//fun Application.configureDatabase() {
//    val log = LoggerFactory.getLogger("Database")
//
//    val config = environment.config
//    val url = config.propertyOrNull("db.mysql.url")?.getString() ?: error("Database URL not found")
//    val user = config.propertyOrNull("db.mysql.user")?.getString() ?: error("Database User not found")
//    val password = config.propertyOrNull("db.mysql.password")?.getString() ?: error("Database Password not found")
//    val driver = config.propertyOrNull("db.mysql.driver")?.getString() ?: "com.mysql.cj.jdbc.Driver"
//
//    try {
//        Database.connect(url, driver, user, password)
//        log.info("✅ Conexión a la base de datos establecida correctamente")
//
//        transaction {
//            SchemaUtils.createMissingTablesAndColumns(Users) // Crea la tabla si no existe
//            val usersCount = Users.selectAll().count()
//            log.info("📊 Usuarios en la base de datos: $usersCount")
//        }
//    } catch (e: Exception) {
//        log.error("❌ Error al conectar con la base de datos: ${e.message}")
//    }
//}
