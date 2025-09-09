package com.example.domain.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class IngresoMensualDTO(
    val mes: Int,
    @Contextual val total: BigDecimal,
    val pagos: Long
)

@Serializable
data class IngresoAnualDTO(
    val anio: Int,
    @Contextual val total: BigDecimal,
    val pagos: Long
)

@Serializable
data class IngresoCobradorDTO(
    val cobradorId: Int,
    val nombre: String,
    @Contextual val total: BigDecimal,
    val pagos: Long
)

@Serializable
data class IngresoMetodoDTO(
    val metodoPagoId: Int,
    val nombre: String,
    @Contextual val total: BigDecimal,
    val pagos: Long
)

@Serializable
data class MatrizFilaDTO(
    val cobradorId: Int,
    val cobrador: String,
    val totalesPorMetodo: List<@Contextual BigDecimal>, // 👈 elementos anotados
    @Contextual val totalFila: BigDecimal
)

@Serializable
data class MatrizCobradorMetodoDTO(
    val metodos: List<String>,
    val filas: List<MatrizFilaDTO>,
    val totalPorMetodo: List<@Contextual BigDecimal>,    // 👈 elementos anotados
    @Contextual val granTotal: BigDecimal
)

@Serializable
data class ReporteIngresosDTO(
    val desde: String,
    val hastaExcl: String,
    val filtro: Map<String, String?>,
    @Contextual val total: BigDecimal,
    val cantidadPagos: Long,
    @Contextual val promedioPorPago: BigDecimal,
    val anual: IngresoAnualDTO,
    val meses: List<IngresoMensualDTO>,
    val porCobrador: List<IngresoCobradorDTO>,
    val porMetodo: List<IngresoMetodoDTO>,
    val cruceCobradorMetodo: MatrizCobradorMetodoDTO
)
