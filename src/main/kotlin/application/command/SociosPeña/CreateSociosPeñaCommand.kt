package com.example.application.command.SociosPeña


import kotlinx.serialization.Serializable


@Serializable
class CreateSociosPeñaCommand(
    val nombre: String,
    val precio: Int,
) {
    fun validate(): CreateSociosPeñaCommand {
        if (nombre.isEmpty()) {
            throw IllegalArgumentException("El nombre de socio no debe ser vacío.")
        }

        if (precio <= 0) { // Validación: precio debe ser mayor a 0
            throw IllegalArgumentException("El precio debe ser un valor positivo mayor a cero.")
        }

        return this
    }
}