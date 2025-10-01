package com.example.domain.entities


import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal

object AlquilerSalones : Table("alquileres_salones") {
    val alquilerId= integer("alquiler_id").autoIncrement()
    val salonId = integer("salon_id").references(Salones.salonId)
    val dni= varchar("dni",10)
    val nombre = varchar("nombre", 100)

    val telefono = varchar("telefono", 20)
    val fecha = datetime("fecha")
    val observaciones = text("observaciones")
    val monto = decimal("monto", 10, 2)
    val condicion = bool("condicion")
    val metodoPagoId = integer("metodo_pago_id").references(metodosPago.metodoPagoId)

    override val primaryKey = PrimaryKey(alquilerId)
}

@Serializable
data class AlquilerSalon(
    val alquilerId: Int? = null,
    val salonId: Int,
    val nombre: String,
    val dni: String,
    val telefono: String,
    @Contextual val fecha: LocalDateTime,
    val observaciones: String,
    @Contextual val monto: BigDecimal,
    val condicion: Boolean,
    val metodoPagoId: Int
) {
    companion object {
        fun create(
            salonId: Int,
            nombre: String,
            dni: String,
            telefono: String,
            fecha: LocalDateTime,
            observaciones: String,
            monto: BigDecimal,
            condicion: Boolean,
            metodoPagoId: Int
        ): AlquilerSalon {
            return AlquilerSalon(
                alquilerId = null,
                salonId = salonId,
                nombre = nombre,
                telefono = telefono,
                fecha = fecha,
                observaciones = observaciones,
                monto = monto,
                condicion = condicion,
                metodoPagoId = metodoPagoId,
                dni= dni
            )
        }
    }
}
