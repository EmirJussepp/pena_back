package com.example.domain.dto


import kotlinx.serialization.Serializable
import java.time.LocalDateTime

import com.example.domain.entities.LocalDateTimeSerializer

@Serializable
data class CuotaDTO(
    val cuotaId: Int,
    val socioId: Int,
    val nombreSocio: String,
    val dni: String,
    val monto: Double,
    @Serializable(with = LocalDateTimeSerializer::class)
    val fechaVencimiento: LocalDateTime,
    val estado: Boolean,
    val direccionSocio: String,
    val telefonoSocio: String
)
