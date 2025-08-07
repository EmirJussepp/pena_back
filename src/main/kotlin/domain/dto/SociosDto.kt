package com.example.domain.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class SocioDTO(
    val socioId: Int,
    val nombre: String,
    val apellido: String,
    val alias: String?,
    val email: String,
    val telefono: String,
    val dni: String,
    val estado:Boolean,
    val numSocioBoca: Int?, // ← si es opcional
    val fechaInicio: LocalDateTime, // ← usando kotlinx.datetime
    val direccion: String?,
    val fechaDeBaja: LocalDateTime?,
    val cobradorNombre: String,
    val tipoPeñaNombre: String,
    val tipoBocaNombre: String?= null,
    val localidadNombre: String
)