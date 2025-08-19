package com.example.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocalidadSyncReq(
    val nombre: String,
    val provincia: String,
    val codigoPostal: String? = ""
)

@Serializable
data class LocalidadSyncRes(val localidadId: Int)