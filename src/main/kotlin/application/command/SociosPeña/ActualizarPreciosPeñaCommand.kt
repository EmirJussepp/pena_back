package com.example.application.command.SociosPeña



import kotlinx.serialization.Serializable


@Serializable
data class ActualizarPrecioPeñaCommand(
    val tipoSocioPeñaId: Int,
    val nombre: String,
    val precio: Int
) {
    fun validate(): ActualizarPrecioPeñaCommand {
        if (tipoSocioPeñaId <= 0) throw IllegalArgumentException("El ID debe ser válido y mayor que cero.")
        if (nombre.isBlank()) throw IllegalArgumentException("El nombre no puede estar vacío.")
        if (precio <= 0) throw IllegalArgumentException("El precio debe ser mayor a cero.")
        return this
    }
}


