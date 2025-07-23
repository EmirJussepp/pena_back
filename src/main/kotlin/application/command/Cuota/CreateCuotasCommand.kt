
package com.example.application.command.Cuota

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CrearCuotaCommand(
    val socioId: Int
) {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        if (socioId <= 0) errors.add("El ID del socio no es válido.")
//        if (fechaVencimiento < LocalDateTime(2023, 1, 1, 0, 0)) errors.add("La fecha de vencimiento no puede ser anterior a 2023.")
        return errors
    }
}
