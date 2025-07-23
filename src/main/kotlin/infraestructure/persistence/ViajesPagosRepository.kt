package com.example.infrastructure.persistence

import com.example.domain.contracts.IViajesPagosContract
import com.example.domain.entities.ViajeBombonera
import com.example.domain.entities.ViajePago

import com.example.domain.entities.ViajesPagos
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ViajesPagosRepository(private val database: Database) : IViajesPagosContract {

    override fun save(viajePago: ViajePago): ViajePago {
        return transaction(database) {
            val insertedId = ViajesPagos.insert {
                it[viajeId] = viajePago.viajeId
                it[monto] = viajePago.monto
                it[nombre] = viajePago.nombre
                it[apellido] = viajePago.apellido
                it[dni] = viajePago.dni
                it[metodoPagoId] = viajePago.metodoPagoId
            }get ViajesPagos.viajeId

            val row = ViajesPagos.select { ViajesPagos.viajePagoId eq insertedId }.single()

            ViajePago(
                viajePagoId = row[ViajesPagos.viajePagoId],
                viajeId = row[ViajesPagos.viajeId],
                monto = row[ViajesPagos.monto],
                nombre = row[ViajesPagos.nombre],
                apellido = row[ViajesPagos.apellido],
                dni = row[ViajesPagos.dni],
                metodoPagoId = row[ViajesPagos.metodoPagoId]
            )
        }
    }
    override fun findByViajeId(viajeId: Int): List<ViajePago> {
        return transaction(database) {
            ViajesPagos
                .select { ViajesPagos.viajeId eq viajeId }
                .map { row ->
                    ViajePago(
                        viajePagoId = row[ViajesPagos.viajePagoId],
                        viajeId = row[ViajesPagos.viajeId],
                        monto = row[ViajesPagos.monto],
                        nombre = row[ViajesPagos.nombre],
                        apellido = row[ViajesPagos.apellido],
                        dni = row[ViajesPagos.dni],
                        metodoPagoId = row[ViajesPagos.metodoPagoId]
                    )
                }
        }
    }

    override fun findAll(): List<ViajePago> {
        return transaction(database) {
            ViajesPagos.selectAll()
                .map { row ->
                    ViajePago(
                        viajePagoId = row[ViajesPagos.viajePagoId],
                        viajeId = row[ViajesPagos.viajeId],
                        monto = row[ViajesPagos.monto],
                        nombre = row[ViajesPagos.nombre],
                        apellido = row[ViajesPagos.apellido],
                        dni = row[ViajesPagos.dni],
                        metodoPagoId = row[ViajesPagos.metodoPagoId]
                    )
                }
        }
    }



}




