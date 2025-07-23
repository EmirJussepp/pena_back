package com.example.infraestructure.persistence

import com.example.domain.contracts.IPagoRepository

import com.example.domain.entities.Pago
import com.example.domain.entities.Pagos



import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.*

import org.jetbrains.exposed.sql.transactions.transaction


class PagoRepository(private val database: Database) : IPagoRepository {
    init {
        transaction(database) {
            SchemaUtils.create(Pagos) // Crea la tabla si no existe
        }
    }

    // Guardar un pago en la base de datos
    override fun guardar(pago: Pago) {
        transaction {
            // Insertar un nuevo pago
            Pagos.insert {
                it[socioId] = pago.socioId
                it[cuotaId] = pago.cuotaId
                it[monto] = pago.monto
                it[metodoPagoId] = pago.metodoPagoId
                it[fechaPago] = pago.fechaPago.toJavaLocalDateTime()
            }

        }
    }
    override fun findAll(): List<Pago> { return transaction {
        Pagos.selectAll().map {
            Pago(
                socioId = it[Pagos.socioId],
                cuotaId = it[Pagos.cuotaId],
                monto= it[Pagos.monto],
                metodoPagoId= it[Pagos.metodoPagoId],
                fechaPago =it[Pagos.fechaPago].toKotlinLocalDateTime()
            )
        }
    }

    }
    override fun existePagoParaCuota(socioId: Int, cuotaId: Int): Boolean {
        return transaction {
            Pagos.select {
                (Pagos.socioId eq socioId) and (Pagos.cuotaId eq cuotaId)
            }.count() > 0
        }
    }
    override fun findById(pagoId: Int): Pago? {
        return transaction(database) {
            Pagos.select { Pagos.pagoId eq pagoId }
                .map { row ->
                    Pago(
                        pagoId = row[Pagos.pagoId],
                        socioId = row[Pagos.socioId],
                        cuotaId = row[Pagos.cuotaId],
                        monto = row[Pagos.monto],
                        metodoPagoId = row[Pagos.metodoPagoId],
                        fechaPago = row[Pagos.fechaPago].toKotlinLocalDateTime()
                    )
                }
                .singleOrNull()
        }
    }





}


    // Obtener pagos por socioId
//    override fun obtenerPagosPorSocio(socioId: Int): List<Pago> {
//        return transaction {
//            PagoEntity.find { PagoTable.socioId eq socioId }
//                .map { it.toPago() }
//        }
//    }
//
//    // Obtener pagos pendientes o filtrados por otras condiciones si es necesario
//    override fun obtenerPagosPendientes(): List<Pago> {
//        return transaction {
//            PagoEntity.find { PagoTable.estado eq false }
//                .map { it.toPago() }
//        }
//    }
