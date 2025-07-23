package com.example.application.command.Salones

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal


@Serializable
data class CreateSalonCommand(
    val nombre: String,
    @Contextual val precio: BigDecimal
) {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        if (nombre.isBlank()) errors.add("El nombre es obligatorio")

        return errors
    }
}
