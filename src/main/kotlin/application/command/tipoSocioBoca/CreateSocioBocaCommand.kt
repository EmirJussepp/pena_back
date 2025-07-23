package com.example.application.command.tipoSocioBoca

import kotlinx.serialization.Serializable


@Serializable
class CreateSocioBocaCommand(
    val nombre: String,
    val precio: Int,
) {
    fun validate(): CreateSocioBocaCommand {
        if (nombre.isEmpty()) {
            throw IllegalArgumentException("El nombre de socio no debe ser vacío.")
        }

        if (precio <= 0) { // Validación: precio debe ser mayor a 0
            throw IllegalArgumentException("El precio debe ser un valor positivo mayor a cero.")
        }
//
//        if (precio > 1_000_000) { // Validación: evitar valores demasiado altos
//            throw IllegalArgumentException("El precio no puede ser mayor a 1,000,000.")
//        }

        return this
    }
}
