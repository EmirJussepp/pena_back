package com.example.application.command.AlquilerSalon

import com.example.domain.entities.BigDecimalSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class CreateAlquilerSalonCommand(
    val salonId: Int,
    val nombre: String,
    val dni: String,
    val telefono: String,
    val fecha: LocalDateTime,
    val observaciones: String,

    @Serializable(with = BigDecimalSerializer::class)
    val monto: BigDecimal,

    val condicion: Boolean,
    val metodoPagoId: Int
)
