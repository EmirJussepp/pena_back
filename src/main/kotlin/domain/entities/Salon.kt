package com.example.domain.entities

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import java.math.BigDecimal

object Salones : Table("salones") {
    val salonId = integer("salon_id").autoIncrement()
    val nombre = varchar("nombre",100)
    val precio= decimal("precio",10,2)

    override val primaryKey = PrimaryKey(salonId)
}

@Serializable
data class Salon(
    val salonId: Int? = null,
    val nombre : String,
 @Contextual val precio: BigDecimal,
) {
    companion object {
        fun create(
            nombre: String,
            precio: BigDecimal

        ): Salon {
            return Salon(
                salonId = null,
                nombre = nombre,
                precio = precio,
            )
        }
    }

}