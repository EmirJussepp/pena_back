package com.example.domain.Dto


import com.example.domain.entities.LocalDateTimeSerializer
import java.time.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class BeneficioConSocioDTO(
    val beneficiosId: Int,
    val socioId: Int,
    val nombre: String,
    val apellido: String,
    val dni: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val fechaOtorgado: LocalDateTime
)
@Serializable
data class PaginadoBeneficiosResponse(
    val beneficios: List<BeneficioConSocioDTO>,
    val total: Long
)
