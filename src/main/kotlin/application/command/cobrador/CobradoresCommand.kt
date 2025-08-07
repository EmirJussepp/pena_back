package com.example.application.command.cobrador

import kotlinx.serialization.Serializable
@Serializable
class CreateCobradorCommand(
    val nombre: String,
    val telefono:String,
    val dni: String,
    val zona: String,

) {
    fun validate(): CreateCobradorCommand {
        if (nombre.isBlank()) {
            throw IllegalArgumentException("El nombre del cobrador no debe estar vacío.")
        }
        if (dni.isBlank() || dni.length !in 7..10) {
            throw IllegalArgumentException("El DNI debe tener entre 7 y 10 caracteres.")
        }
        if (zona.isBlank()) {
            throw IllegalArgumentException("La zona del cobrador no debe estar vacía.")
        }
        return this
    }
}