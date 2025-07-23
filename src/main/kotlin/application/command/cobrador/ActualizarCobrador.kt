package com.example.application.command.cobrador

import kotlinx.serialization.Serializable

@Serializable
data class ActualizarCobrador(
    val nombre: String,
    val telefono: String,
    val dni: String,
    val zona: String
)
