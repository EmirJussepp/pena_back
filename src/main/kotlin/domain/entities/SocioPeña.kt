package com.example.domain.entities




import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable

// Definimos la tabla Users en la base de datos
object TiposSocioPeña : Table("tipo_socio") {
    val tipoSocioPeñaId= integer("tipo_id_socioPeña").autoIncrement() // ID autoincremental
    val nombre = varchar("nombre", 100)
    val precio = integer("precio")

    override val primaryKey = PrimaryKey(tipoSocioPeñaId)
}

@Serializable
data class TipoSocioPeña(
    val tipoSocioPeñaId: Int? = null, // Puede ser nulo al crearse (autoincremental)
    val nombre: String,
    val precio: Int,

    ) {
    companion object {
        fun create(
            nombre: String,
            precio: Int,

            ): TipoSocioPeña {
            return TipoSocioPeña(
                tipoSocioPeñaId = null,
                nombre = nombre,
                precio = precio,

                )
        }
    }
}