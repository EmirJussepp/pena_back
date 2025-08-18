package com.example.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class ViajeBomboneraFiltroResponse(
    val viajes: List<ViajeBomboneraDto>,
    val total: Int
)