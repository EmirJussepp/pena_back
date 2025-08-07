package com.example.domain.dto

data class ViajePagoDTO(
    val socioId: Int?,
    val viajeId: Int,
    val monto: Double,
    val nombreNoSocio: String?,
    val apellidoNoSocio: String?,
    val dniNoSocio: String?,
    val esSocio: Boolean,
    val total: Double,
    val metodoPagoId: Int
)
