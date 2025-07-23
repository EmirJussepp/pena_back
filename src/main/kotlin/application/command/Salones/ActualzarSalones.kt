package com.example.application.command.Salones

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class ActualzarSalones(
    val nombre: String,
    @Contextual val precio: BigDecimal

) {
    fun validate(): ActualzarSalones{
        // Validar el comando si querés (por ejemplo, que nombre no sea vacío)
        if (nombre.isBlank()) {
            throw IllegalArgumentException("El nombre no puede estar vacío")
        }
        return this
    }

}