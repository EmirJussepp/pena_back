package com.example.application.command.metodoPago


import kotlinx.serialization.Serializable

@Serializable
class CreateMetodoPagoCommand(
    val nombre: String,
) {
    fun validate(): CreateMetodoPagoCommand {
        if (nombre.isEmpty()) {
            throw IllegalArgumentException("El nombre del método de pago no debe estar vacío.")
        }
        return this
    }
}