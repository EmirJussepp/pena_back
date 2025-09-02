package com.example.infraestructure.persistence

import com.example.domain.entities.ViajeBombonera
import com.example.domain.entities.ViajesBombonera
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import com.example.domain.contracts.IViajesBombonera
import com.example.domain.dto.ViajeBomboneraDto
import com.example.domain.entities.ViajesPagos

import org.jetbrains.exposed.sql.SqlExpressionBuilder.between


import org.jetbrains.exposed.sql.SqlExpressionBuilder.like


import java.time.LocalDate


class ViajeBomboneraRepository(private val database: Database) : IViajesBombonera {

    override fun save(viajeBombonera: ViajeBombonera): ViajeBombonera {
        return transaction(database) {

            // Verificar si ya existe un viaje con la misma fecha y destino (ajustar la lógica según tu necesidad)
            val existingViaje = ViajesBombonera.select {
                ViajesBombonera.fechaViaje eq viajeBombonera.fechaViaje and
                        (ViajesBombonera.destino eq viajeBombonera.destino)
            }
                .singleOrNull()

            if (existingViaje == null) {
                // Insertar nuevo viaje
                val insertedId = ViajesBombonera.insert {
                    it[fechaViaje] = viajeBombonera.fechaViaje
                    it[destino] = viajeBombonera.destino

                } get ViajesBombonera.viajeBomboneraId

                // Obtener el viaje recién insertado
                ViajesBombonera.select { ViajesBombonera.viajeBomboneraId eq insertedId }.map { row ->
                    ViajeBombonera(
                        viajeBomboneraId = row[ViajesBombonera.viajeBomboneraId],
                        fechaViaje = row[ViajesBombonera.fechaViaje],
                        destino = row[ViajesBombonera.destino],

                        )
                }.first()
            } else {
                // Si existe, actualizamos el viaje
                ViajesBombonera.update({ ViajesBombonera.viajeBomboneraId eq ViajesBombonera.viajeBomboneraId }) {
                    it[fechaViaje] = viajeBombonera.fechaViaje
                    it[destino] = viajeBombonera.destino

                }
                // Retornar el objeto actualizado
                viajeBombonera.copy(viajeBomboneraId = existingViaje[ViajesBombonera.viajeBomboneraId])
            }
        }
    }

    override fun findById(viajeBomboneraId: Int): ViajeBombonera? {
        return transaction(database) {
            ViajesBombonera
                .select { ViajesBombonera.viajeBomboneraId eq viajeBomboneraId }
                .mapNotNull { row ->
                    ViajeBombonera(
                        viajeBomboneraId = row[ViajesBombonera.viajeBomboneraId],
                        fechaViaje = row[ViajesBombonera.fechaViaje],
                        destino = row[ViajesBombonera.destino]
                    )
                }
                .singleOrNull()
        }
    }

    // Listar todos
    override fun findAll(): List<ViajeBombonera> {
        return transaction(database) {
            ViajesBombonera.selectAll()
                .orderBy(ViajesBombonera.fechaViaje to SortOrder.DESC)
                .map {
                    ViajeBombonera(
                        viajeBomboneraId = it[ViajesBombonera.viajeBomboneraId],
                        fechaViaje = it[ViajesBombonera.fechaViaje],
                        destino = it[ViajesBombonera.destino]
                    )
                }
        }
    }

    // Función interna para generar filtros
    private fun generarCondiciones(filtro: String?, mesAnio: String?): Op<Boolean>? {
        val condiciones = mutableListOf<Op<Boolean>>()

        filtro?.takeIf { it.isNotBlank() }?.let { texto ->
            condiciones.add(
                (ViajesBombonera.destino.lowerCase() like "%${texto.lowercase()}%") or
                        (ViajesBombonera.fechaViaje.castTo<String>(VarCharColumnType()).like("%$texto%"))
            )
        }

        mesAnio?.takeIf { it.isNotBlank() }?.let { filtroMes ->
            val partes = filtroMes.split("/")
            if (partes.size == 2) {
                val mes = partes[0].toIntOrNull()
                val anio = partes[1].toIntOrNull()
                if (mes != null && anio != null) {
                    // Crear rango de fechas para ese mes
                    val fechaInicio = LocalDate.of(anio, mes, 1)
                    val fechaFin = fechaInicio.withDayOfMonth(fechaInicio.lengthOfMonth())
                    condiciones.add(
                        (ViajesBombonera.fechaViaje.between(fechaInicio, fechaFin))
                    )
                }
            }
        }


        return condiciones.reduceOrNull { acc, op -> acc and op }
    }

    // Contar viajes según filtros
    fun contarViajes(filtro: String? = null, mesAnio: String? = null): Int {
        return transaction(database) {
            val condicionFinal = generarCondiciones(filtro, mesAnio)
            val query = if (condicionFinal != null) {
                ViajesBombonera.select { condicionFinal }
            } else {
                ViajesBombonera.selectAll()
            }
            query.count().toInt() // count() devuelve Long, lo convertimos a Int
        }
    }


    // Buscar viajes con paginación y filtros
//    fun buscarViajes(
//        filtro: String? = null,
//        mesAnio: String? = null,
//        pagina: Int = 1,
//        tamanioPagina: Int = 3
//    ): List<ViajeBombonera> {
//        return transaction(database) {
//            val condicionFinal = generarCondiciones(filtro, mesAnio)
//            val query = if (condicionFinal != null) {
//                ViajesBombonera.select { condicionFinal }
//            } else {
//                ViajesBombonera.selectAll()
//            }
//
//            query.orderBy(ViajesBombonera.fechaViaje to SortOrder.DESC)
//                .limit(tamanioPagina, offset = ((pagina - 1) * tamanioPagina).toLong())
//                .map {
//                    ViajeBombonera(
//                        viajeBomboneraId = it[ViajesBombonera.viajeBomboneraId],
//                        fechaViaje = it[ViajesBombonera.fechaViaje],
//                        destino = it[ViajesBombonera.destino],
//
//
//                    )
//                }
//        }
//    }

    override fun update(viajeBombonera: ViajeBombonera): ViajeBombonera {
        return transaction(database) {
            ViajesBombonera.update({ ViajesBombonera.viajeBomboneraId eq viajeBombonera.viajeBomboneraId!! }) {
                it[fechaViaje] = viajeBombonera.fechaViaje
                it[destino] = viajeBombonera.destino
                // solo los campos de la tabla ViajesBombonera
            }
            viajeBombonera
        }
    }
    fun buscarViajesConTotales(
        filtro: String? = null,
        mes: Int? = null,  // mejor usar Int para filtrar por mes
        pagina: Int = 1,
        tamanioPagina: Int = 3
    ): List<ViajeBomboneraDto> {
        return transaction(database) {
            val condicionFinal = generarCondiciones(filtro, mes.toString())

            val query = if (condicionFinal != null) {
                ViajesBombonera.select { condicionFinal }
            } else {
                ViajesBombonera.selectAll()
            }

            query.orderBy(ViajesBombonera.fechaViaje to SortOrder.DESC)
                .limit(tamanioPagina, offset = ((pagina - 1) * tamanioPagina).toLong())
                .map { row ->
                    val viajeId = row[ViajesBombonera.viajeBomboneraId]

                    // Calcular totales de pasajeros y monto
                    val pagos = ViajesPagos.select { ViajesPagos.viajeId eq viajeId }
                    val totalPasajeros = pagos.count()
                    val totalMonto = pagos.sumOf { it[ViajesPagos.monto].toDouble() }

                    ViajeBomboneraDto(
                        viajeBomboneraId = viajeId,
                        fechaViaje = row[ViajesBombonera.fechaViaje].toString(),
                        destino = row[ViajesBombonera.destino],
                        totalPasajeros = totalPasajeros.toInt(),
                        totalMonto = totalMonto
                    )
                }

        }
    }





}