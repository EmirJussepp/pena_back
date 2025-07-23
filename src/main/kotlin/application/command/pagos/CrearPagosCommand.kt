package com.example.application.command.pagos

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class CrearPagoCommand(
    val socioId: Int,
    val cuotaId: List<Int>,
    val metodoPagoId: Int
) {
    fun validate(): List<String> {
        val errores = mutableListOf<String>()
        if (socioId <= 0) errores.add("ID de socio inválido.")
        if (cuotaId.isEmpty()) errores.add("Debes seleccionar al menos una cuota")
//        if (monto <= BigDecimal.ZERO) errores.add("El monto debe ser mayor a cero.")
        if (metodoPagoId <= 0) errores.add("Método de pago inválido.")
        return errores
    }
}