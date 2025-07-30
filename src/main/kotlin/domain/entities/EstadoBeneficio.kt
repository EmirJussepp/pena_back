package com.example.domain.entities

data class EstadoBeneficio(
    val cuotasPagadas: Int,
    val cuotasImpagasVencidas: Int,
    val socioActivo: Boolean,
    val tieneBeneficio: Boolean
)
