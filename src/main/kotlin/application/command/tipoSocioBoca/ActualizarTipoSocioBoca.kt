package com.example.application.command.tipoSocioBoca

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTipoSocioBocaCommand(
    val tipoSocioBocaId: Int, // Para identificar qué tipo actualizar
    val nombre: String,
    val precio: Int
    // otros campos que tenga TipoSocioBoca y quieras actualizar
) {
    fun validate(): UpdateTipoSocioBocaCommand{
        // Validar el comando si querés (por ejemplo, que nombre no sea vacío)
        if (nombre.isBlank()) {
            throw IllegalArgumentException("El nombre no puede estar vacío")
        }
        if (precio < 0) {
            throw IllegalArgumentException("El precio no puede ser negativo")
        }
        return this
    }

}
