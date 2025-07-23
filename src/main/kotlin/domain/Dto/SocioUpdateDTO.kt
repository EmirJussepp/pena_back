package com.example.domain.Dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SocioUpdateDTO(
    val socioId: Int,
    val nombre: String,
    val alias: String?,
    val apellido: String,
    val email: String,
    val dni: String,
    val numSocioBoca: Int?,
    val telefono: String?,
    val cobradorId: Int,
    val tipoPeñaId: Int,
    val tipoBocaId: Int,
    val userId: Int,
    val localidadId: Int,
    val estado: Boolean,
    val direccion: String?,
    val fechaDeBaja: LocalDateTime? = null,

)

