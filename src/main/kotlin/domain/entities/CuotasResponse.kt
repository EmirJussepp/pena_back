package com.example.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class RespuestaCuotas(
    val cuotasPendientes: List<Map<String, String>>,
    val totalAdeudado: String,
    val cantidadCuotasPendientes: Int
)
