package com.example.application.command.Movimientos




import com.example.domain.entities.BigDecimalSerializer
import java.math.BigDecimal
import kotlinx.datetime.LocalDateTime

import kotlinx.serialization.Contextual

import kotlinx.serialization.Serializable

@Serializable
data class CrearMovimientoCommand(
    @Serializable(with = BigDecimalSerializer::class)
    @Contextual val monto: BigDecimal,
    val descripcion: String,
    val tipo: String, // "ingreso" o "egreso"
    val fecha: LocalDateTime, // Para la fecha del movimiento
    val metodoPagoId: Int
) {
    fun validate(): CrearMovimientoCommand {
        require(monto > BigDecimal.ZERO) { "El monto debe ser mayor que 0." }
        require(descripcion.isNotBlank()) { "La descripción no puede estar vacía ni en blanco." }
        require(tipo in listOf("ingreso", "egreso")) { "El tipo debe ser 'ingreso' o 'egreso'." }

        return this
    }
}