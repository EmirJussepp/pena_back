package com.example.domain.entities

import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable

// Definimos la tabla Users en la base de datos
object Localidades : Table("localidades") {
    val localidadId= integer("localidad_id").autoIncrement() // ID autoincremental
    val nombre = varchar("nombre", 100)
    val provincia = varchar("provincia", 100)
    val codigoPostal = varchar("codigo_postal", 255) // Debería estar encriptada
    override val primaryKey = PrimaryKey(localidadId)
}

@Serializable
data class Localidad(
    val localidadId: Int? = null, // Puede ser nulo al crearse (autoincremental)
    val nombre: String,
    val provincia: String,
    val codigoPostal: String
) {
    companion object {
        fun create(
            nombre: String,
            provincia: String,
            codigoPostal: String
        ): Localidad {
            return Localidad(
                localidadId = null, // La BD generará el ID
                nombre = nombre,
                provincia = provincia,
                codigoPostal = codigoPostal
            )
        }
    }
}