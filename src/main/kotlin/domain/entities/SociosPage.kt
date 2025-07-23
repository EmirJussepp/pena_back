package com.example.domain.entities

import com.example.domain.Dto.SocioDTO
import kotlinx.serialization.Serializable

@Serializable
data class SociosPage(
    val total: Int,
    val socios: List<SocioDTO>,
    val page: Int,
    val size: Int
)