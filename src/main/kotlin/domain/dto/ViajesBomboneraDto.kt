package com.example.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class ViajeBomboneraDto(
    val viajeBomboneraId: Int?,
    val fechaViaje: String, // o LocalDate si serializas LocalDate
    val destino: String
)