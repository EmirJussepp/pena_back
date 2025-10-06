package com.example.infraestructure.persistence


import com.example.domain.dto.ViajePagoFullDTO
import com.example.domain.contracts.IViajesPagosContract
import com.example.domain.dto.PaginacionViajePagoDTO
import com.example.domain.entities.*


import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ViajesPagosRepository(private val database: Database) : IViajesPagosContract {
    init {
        transaction(database) {
            SchemaUtils.create(ViajesPagos) // Crea la tabla si no existe
        }
    }
    override fun save(viajePago: ViajePago): ViajePago {
        return transaction(database) {
            val insertedId = ViajesPagos.insert {
                it[viajeId] = viajePago.viajeId
                it[monto] = viajePago.monto
                it[nombre] = viajePago.nombre
                it[apellido] = viajePago.apellido
                it[dni] = viajePago.dni
                it[metodoPagoId] = viajePago.metodoPagoId
                it[cobradorId] = viajePago.cobradorId
            } get ViajesPagos.viajePagoId
            val row = ViajesPagos.select { ViajesPagos.viajePagoId eq insertedId }.singleOrNull()
                ?: error("❌ No se encontró el viaje pago con ID $insertedId luego de insertarlo")


            ViajePago(
                viajePagoId = row[ViajesPagos.viajePagoId],
                viajeId = row[ViajesPagos.viajeId],
                monto = row[ViajesPagos.monto],
                nombre = row[ViajesPagos.nombre],
                apellido = row[ViajesPagos.apellido],
                dni = row[ViajesPagos.dni],
                metodoPagoId = row[ViajesPagos.metodoPagoId],
                cobradorId = row[ViajesPagos.cobradorId]
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
                        metodoPagoId = row[ViajesPagos.metodoPagoId],
                        cobradorId = row[ViajesPagos.cobradorId]
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
                        metodoPagoId = row[ViajesPagos.metodoPagoId],
                        cobradorId = row[ViajesPagos.cobradorId]
                    )
                }
        }
    }

//    fun findAllConCobradorYMetodo(): List<ViajePagoFullDTO> {
//        return transaction(database) {
//            (ViajesPagos innerJoin Cobradores innerJoin metodosPago)
//                .selectAll()
//                .map { row ->
//                    ViajePagoFullDTO(
//                        viajePagoId = row[ViajesPagos.viajePagoId],
//                        viajeId = row[ViajesPagos.viajeId],
//                        monto = row[ViajesPagos.monto].toDouble(),
//                        nombre = row[ViajesPagos.nombre]!!,
//                        apellido = row[ViajesPagos.apellido]!!,
//                        dni = row[ViajesPagos.dni]!!,
//                        metodoPagoId = row[ViajesPagos.metodoPagoId],
//                        metodoPagoNombre = row[metodosPago.nombre],
//                        cobradorId = row[ViajesPagos.cobradorId],
//                        cobradorNombre = row[Cobradores.nombre]
////                        cobradorApellido = row[Cobradores.apellido]
//                    )
//                }
//        }
//    }

    override suspend fun eliminarPorId(viajePagoId: Int): Boolean {
        return transaction(database) {
            val deletedCount = ViajesPagos.deleteWhere { ViajesPagos.viajePagoId eq viajePagoId }
            deletedCount > 0
        }
    }

    override fun update(viajePago: ViajePago): ViajePago {
        return transaction(database) {
            ViajesPagos.update({ ViajesPagos.viajePagoId eq viajePago.viajePagoId!! }) {
                it[monto] = viajePago.monto
                it[nombre] = viajePago.nombre
                it[apellido] = viajePago.apellido
                it[dni] = viajePago.dni
                it[metodoPagoId] = viajePago.metodoPagoId
                it[cobradorId] = viajePago.cobradorId
                // No actualizamos viajeId ni viajePagoId
            }
            viajePago
        }
    }
//
//    fun findByViajeIdPaginado(viajeId: Int, page: Int, pageSize: Int): List<ViajePago> {
//        return transaction(database) {
//            ViajesPagos
//                .select { ViajesPagos.viajeId eq viajeId }
//                .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
//                .map { row ->
//                    ViajePago(
//                        viajePagoId = row[ViajesPagos.viajePagoId],
//                        viajeId = row[ViajesPagos.viajeId],
//                        monto = row[ViajesPagos.monto],
//                        nombre = row[ViajesPagos.nombre],
//                        apellido = row[ViajesPagos.apellido],
//                        dni = row[ViajesPagos.dni],
//                        metodoPagoId = row[ViajesPagos.metodoPagoId],
//                        cobradorId = row[ViajesPagos.cobradorId]
//                    )
//                }
//        }
//    }

//    fun countByViajeId(viajeId: Int): Long {
//        return transaction(database) {
//            ViajesPagos
//                .select { ViajesPagos.viajeId eq viajeId }
//                .count()
//        }
//    }

    //    override fun findByViajeIdConPaginacionFull(viajeId: Int, page: Int, pageSize: Int): PaginacionViajePagoDTO {
//        return transaction(database) {
//            val offset = ((page - 1) * pageSize).toLong()
//
//            val pasajeros = (ViajesPagos innerJoin Cobradores innerJoin metodosPago)
//                .select { ViajesPagos.viajeId eq viajeId }
//                .limit(pageSize, offset)
//                .map { row ->
//                    ViajePagoFullDTO(
//                        viajePagoId = row[ViajesPagos.viajePagoId],
//                        viajeId = row[ViajesPagos.viajeId],
//                        monto = row[ViajesPagos.monto].toDouble(),
//                        nombre = row[ViajesPagos.nombre] ?: "",
//                        apellido = row[ViajesPagos.apellido] ?: "",
//                        dni = row[ViajesPagos.dni] ?: "",
//                        metodoPagoId = row[ViajesPagos.metodoPagoId],
//                        metodoPagoNombre = row[metodosPago.nombre],
//                        cobradorId = row[ViajesPagos.cobradorId],
//                        cobradorNombre = row[Cobradores.nombre]
//                    )
//                }
//
//            val total = ViajesPagos
//                .select { ViajesPagos.viajeId eq viajeId }
//                .count()
//
//            PaginacionViajePagoDTO(
//                pasajeros = pasajeros,
//                total = total,
//                page = page,
//                pageSize = pageSize
//            )
//        }
//    }
    override fun findByViajeIdConPaginacionFull(viajeId: Int, page: Int, pageSize: Int): PaginacionViajePagoDTO {
        return transaction(database) {
            val offset = ((page - 1) * pageSize).toLong()

            // Lista de pasajeros con JOIN a cobradores y métodos de pago
            val pasajeros = (ViajesPagos innerJoin Cobradores innerJoin metodosPago)
                .select { ViajesPagos.viajeId eq viajeId }
                .limit(pageSize, offset)
                .map { row ->
                    ViajePagoFullDTO(
                        viajePagoId = row[ViajesPagos.viajePagoId],
                        viajeId = row[ViajesPagos.viajeId],
                        monto = row[ViajesPagos.monto].toDouble(),
                        nombre = row[ViajesPagos.nombre] ?: "",
                        apellido = row[ViajesPagos.apellido] ?: "",
                        dni = row[ViajesPagos.dni] ?: "",
                        metodoPagoId = row[ViajesPagos.metodoPagoId],
                        metodoPagoNombre = row[metodosPago.nombre],
                        cobradorId = row[ViajesPagos.cobradorId],
                        cobradorNombre = row[Cobradores.nombre]
                    )
                }

            // Cantidad total de pasajeros (sin paginar)
            val totalPasajeros = ViajesPagos
                .select { ViajesPagos.viajeId eq viajeId }
                .count()

            // Total de monto de todos los pasajeros del viaje
            val totalMonto = ViajesPagos
                .slice(ViajesPagos.monto.sum())
                .select { ViajesPagos.viajeId eq viajeId }
                .map { it[ViajesPagos.monto.sum()] ?: 0.0 }
                .first()

            PaginacionViajePagoDTO(
                pasajeros = pasajeros,
                total = totalPasajeros,   // sigue sirviendo para paginación
                page = page,
                pageSize = pageSize,
                totalMonto = totalMonto.toDouble(),  // 👈 agregado
                totalPasajeros = totalPasajeros.toInt() // 👈 agregado
            )
        }
    }


}










