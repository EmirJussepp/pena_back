package com.example.domain.entities

import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable

// Definimos la tabla Users en la base de datos
object metodosPago : Table("metodo_pago") {
    val metodoPagoId = integer("metodo_pago_id").autoIncrement() // ID autoincremental
    val nombre = varchar("nombre", 100)

    override val primaryKey = PrimaryKey(metodoPagoId)
}

@Serializable
data class metodoPago (
    val metodoPagoId: Int? = null,
    val nombre: String,

) {
    companion object {
        fun create(
            nombre: String,

        ): metodoPago {
            return metodoPago(
                metodoPagoId = null, // La BD generará el ID
                nombre = nombre,

            )
        }
    }
}
