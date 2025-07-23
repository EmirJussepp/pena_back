package com.example.domain.entities

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class BalanceSemanalResponse(
    val movimientos: List<Movimiento>,
    @Contextual val balanceTotal: BigDecimal,
    @Contextual val totalEfectivo: BigDecimal,
    @Contextual val totalTransferencia: BigDecimal
)
