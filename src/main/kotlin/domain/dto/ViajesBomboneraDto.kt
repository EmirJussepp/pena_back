package com.example.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class ViajeBomboneraDto(
    val viajeBomboneraId: Int?,
    val fechaViaje: String, // o LocalDate si serializas LocalDate
    val destino: String,
    val totalPasajeros: Int = 0,   // 👈 agregado
    val totalMonto: Double = 0.0   // 👈 agregado
)