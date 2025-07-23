package com.example.domain.entities

import com.example.domain.entities.AlquilerSalones.references
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

import kotlinx.datetime.toLocalDateTime

object Movimientos : Table("movimientos") {
    val movimientoId = integer("movimiento_id").autoIncrement()
    val fecha = datetime("fecha")
    val tipo = varchar("tipo", 10) // "ingreso" o "egreso"
    val monto = decimal("monto", 10, 2)
    val descripcion = text("descripcion").nullable()
    val metodoPagoId = integer("metodo_pago_id").references(metodosPago.metodoPagoId)

    override val primaryKey = PrimaryKey(movimientoId)
}

@Serializable
data class Movimiento(
    val movimientoId: Int? = null,
    val fecha: LocalDateTime,
    val tipo: String, // Validar que sea "ingreso" o "egreso"
    @Contextual val monto: BigDecimal,
    val descripcion: String?,
    val metodoPagoId: Int
) {
    companion object {
        fun create(
            fecha: LocalDateTime,
            tipo: String, // Debe ser "ingreso" o "egreso"
            monto: BigDecimal,
            descripcion: String?,
            metodoPagoId: Int

        ): Movimiento {
            return Movimiento(
                movimientoId = null,
                fecha = fecha,
                tipo = tipo,
                monto = monto,
                descripcion = descripcion,
                metodoPagoId= metodoPagoId
            )
        }
    }
}