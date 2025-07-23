package com.example.domain.entities

import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable

// Definimos la tabla Users en la base de datos
object Users : Table("usuarios") {
    val userId = integer("user_id").autoIncrement() // ID autoincremental
    val name = varchar("nombre", 100)
    val email = varchar("email", 100)
    val password = varchar("password", 255) // Debería estar encriptada
    override val primaryKey = PrimaryKey(userId)
}

@Serializable
data class User(
    val userId: Int? = null, // Puede ser nulo al crearse (autoincremental)
    val name: String,
    val email: String,
    val password: String
) {
    companion object {
        fun create(
            name: String,
            email: String,
            password: String
        ): User {
            return User(
                userId = null, // La BD generará el ID
                name = name,
                email = email,
                password = password
            )
        }
    }
}
