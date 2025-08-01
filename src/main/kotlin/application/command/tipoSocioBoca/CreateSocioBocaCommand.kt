package com.example.application.command.tipoSocioBoca

import kotlinx.serialization.Serializable


@Serializable
class CreateSocioBocaCommand(
    val nombre: String

) {
    fun validate(): CreateSocioBocaCommand {
        if (nombre.isEmpty()) {
            throw IllegalArgumentException("El nombre de socio no debe ser vacío.")
        }

        return this
    }
}
