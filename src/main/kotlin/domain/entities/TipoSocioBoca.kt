package com.example.domain.entities


import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable

// Definimos la tabla Users en la base de datos
object TiposSocioBoca : Table("tipo_socioboca") {
    val tipoSocioBocaId= integer("tipo_boca_id").autoIncrement() // ID autoincremental
    val nombre = varchar("nombre", 100)
    val precio = integer("precio")

    override val primaryKey = PrimaryKey(tipoSocioBocaId)
}

@Serializable
data class TipoSocioBoca(
    val tipoSocioBocaId: Int? = null, // Puede ser nulo al crearse (autoincremental)
    val nombre: String,
    val precio: Int,

) {
    companion object {
        fun create(
            nombre: String,
            precio: Int,

        ): TipoSocioBoca {
            return TipoSocioBoca(
                tipoSocioBocaId = null, // La BD generará el ID
                nombre = nombre,
                precio = precio,

            )
        }
    }
}