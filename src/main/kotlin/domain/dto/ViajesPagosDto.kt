package com.example.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class ViajePagoFullDTO(
    val viajePagoId: Int,
    val viajeId: Int,
    val monto: Double,
    val nombre: String,
    val apellido: String,
    val dni: String,
    val metodoPagoId: Int,
    val metodoPagoNombre: String,
    val cobradorId: Int,
    val cobradorNombre: String,
//    val cobradorApellido: String
)
