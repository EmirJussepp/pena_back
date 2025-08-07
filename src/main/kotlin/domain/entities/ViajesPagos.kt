package com.example.domain.entities


import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

import java.math.BigDecimal

object ViajesPagos : Table("viajes_pagos") {
    val viajePagoId = integer("viaje_pago_id").autoIncrement()
    val viajeId = integer("viaje_id").references(ViajesBombonera.viajeBomboneraId)
    val monto = decimal("monto", 10, 2)
    val nombre = varchar("nombre", 255).nullable()
    val apellido = varchar("apellido", 250).nullable()
    val dni = varchar("dni", 255).nullable()
    val cobradoresId= integer("cobrador_id").references(Cobradores.cobradoresId)
    val metodoPagoId = integer("metodo_pago_id").references(metodosPago.metodoPagoId)

    override val primaryKey = PrimaryKey(viajePagoId)
}

@Serializable
data class ViajePago(
    val viajePagoId: Int? = null,
    val viajeId: Int,
    @Contextual val monto: BigDecimal,
    val nombre: String?,
    val apellido: String?,
    val dni: String?,
    val metodoPagoId: Int,
    val cobradoresId: Int
) {
    companion object {
        fun create(
            viajeId: Int,
            monto: BigDecimal,
            nombre: String?,
            apellido: String?,
            dni: String?,
            metodoPagoId: Int,
            cobradoresId: Int
        ): ViajePago {
            return ViajePago(
                viajePagoId = null,

                viajeId = viajeId,
                monto = monto,
                nombre = nombre,
                apellido = apellido,
                dni = dni,
                metodoPagoId = metodoPagoId,
                cobradoresId= cobradoresId
            )
        }
    }
}

