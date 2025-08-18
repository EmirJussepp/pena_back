package com.example.domain.dto

@kotlinx.serialization.Serializable
data class LocalidadSyncReq(
    val nombre: String,
    val provincia: String,
    val codigoPostal: String? = ""
)