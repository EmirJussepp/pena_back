package com.example.infraestructure.persistence

import com.example.domain.entities.ViajeBombonera
import com.example.domain.entities.ViajesBombonera
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import com.example.domain.contracts.IViajesBombonera

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.javatime.month
import org.jetbrains.exposed.sql.javatime.year


class ViajeBomboneraRepository(private val database: Database) : IViajesBombonera {

    override fun save(viajeBombonera: ViajeBombonera): ViajeBombonera {
        return transaction(database) {

            // Verificar si ya existe un viaje con la misma fecha y destino (ajustar la lógica según tu necesidad)
            val existingViaje = ViajesBombonera.select { ViajesBombonera.fechaViaje eq viajeBombonera.fechaViaje and
                    (ViajesBombonera.destino eq viajeBombonera.destino) }
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
                .select { ViajesBombonera.viajeBomboneraId eq viajeBomboneraId } // Filtrar por ID de viaje
                .mapNotNull { row ->
                    // Mapear los datos de la tabla a la entidad ViajeBombonera
                    ViajeBombonera(
                        viajeBomboneraId = row[ViajesBombonera.viajeBomboneraId],
                        fechaViaje = row[ViajesBombonera.fechaViaje],
                        destino = row[ViajesBombonera.destino],

                    )
                }
                .singleOrNull() // Devuelve un solo registro o null si no existe
        }
    }
    override fun findAll(): List<ViajeBombonera> {
        return transaction(database) {
            ViajesBombonera.selectAll()
                .orderBy(ViajesBombonera.fechaViaje to SortOrder.DESC)

                .map {
                    ViajeBombonera(
                        viajeBomboneraId = it[ViajesBombonera.viajeBomboneraId],
                        fechaViaje = it[ViajesBombonera.fechaViaje],
                        destino = it[ViajesBombonera.destino],

                    )
                }
        }
    }
    fun buscarViajes(
        filtro: String? = null,
        mesAnio: String? = null, // formato esperado "MM/YYYY"
        pagina: Int = 1,
        tamanioPagina: Int = 5
    ): List<ViajeBombonera> {
        return transaction(database) {
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
                        condiciones.add(
                            (ViajesBombonera.fechaViaje.month() eq mes) and
                                    (ViajesBombonera.fechaViaje.year() eq anio)
                        )
                    }
                }
            }

            val condicionFinal = condiciones.reduceOrNull { acc, op -> acc and op }

            val query = if (condicionFinal != null) {
                ViajesBombonera.select { condicionFinal }
            } else {
                ViajesBombonera.selectAll()
            }

            query.orderBy(ViajesBombonera.fechaViaje to SortOrder.DESC)
                .limit(tamanioPagina, offset = ((pagina - 1) * tamanioPagina).toLong())
                .map {
                    ViajeBombonera(
                        viajeBomboneraId = it[ViajesBombonera.viajeBomboneraId],
                        fechaViaje = it[ViajesBombonera.fechaViaje],
                        destino = it[ViajesBombonera.destino]
                    )
                }
        }
    }


}
