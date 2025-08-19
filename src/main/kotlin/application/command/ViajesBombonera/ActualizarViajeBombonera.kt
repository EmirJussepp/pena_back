package com.example.application.command.ViajesBombonera

import com.example.application.command.ViajesBombonera.ViajeBomboneraCommand.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ActualizarViajeBombonera(
    val viajeBomboneraId: Int,
    @Serializable(with = LocalDateTimeSerializer::class) val fechaViaje: LocalDateTime,
    val destino: String
)
