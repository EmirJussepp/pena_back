package com.example.domain.entities


import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable



// Definimos la tabla ViajesBombonera en la base de datos
object Cobradores : Table("cobradores") {
    val cobradoresId = integer("cobrador_id").autoIncrement() // ID autoincremental
    val nombre = varchar("nombre", 100)
    val telefono= varchar("telefono", 15)
    val dni = varchar("dni", 10)
    val zona = varchar("zona",150) // Definimos precisión y escala


    override val primaryKey = PrimaryKey(cobradoresId)
}

@Serializable
data class Cobrador(
    val cobradoresId: Int? = null, // Puede ser nulo al crearse (autoincremental)
    val nombre: String,
    val telefono: String,
    val dni: String,
    val zona: String
){
    companion object {
        fun create(
            nombre: String,
            telefono: String,
            dni: String,
            zona: String
        ): Cobrador {
            return Cobrador(
                cobradoresId = null, // La BD generará el ID
                nombre = nombre,
                telefono=telefono,
                dni = dni,
                zona = zona
            )
        }
    }
}
