package com.example.infraestructure.persistence


import com.example.domain.contracts.AlquilerSalonesContract
import com.example.domain.entities.AlquilerSalon
import com.example.domain.entities.AlquilerSalones
import com.example.domain.entities.Movimiento
import com.example.domain.entities.Movimientos

import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class AlquilerSalonesRepository(private val database: Database) : AlquilerSalonesContract {
    init {
        transaction(database) {
            SchemaUtils.create(AlquilerSalones)
        }
    }

    override fun save(alquilerSalon: AlquilerSalon): AlquilerSalon {
        return transaction {
            val insertedId = AlquilerSalones.insert {
                it[nombre] = alquilerSalon.nombre
                it[salonId] = alquilerSalon.salonId
                it[telefono] = alquilerSalon.telefono
                it[fecha] = alquilerSalon.fecha.toJavaLocalDateTime()
                it[observaciones] = alquilerSalon.observaciones
                it[monto] = alquilerSalon.monto
                it[condicion] = alquilerSalon.condicion
                it[metodoPagoId] = alquilerSalon.metodoPagoId
                it[dni] = alquilerSalon.dni
            } get AlquilerSalones.alquilerId

            AlquilerSalones.select { AlquilerSalones.alquilerId eq insertedId }
                .map {
                    AlquilerSalon(
                        alquilerId = it[AlquilerSalones.alquilerId],
                        salonId = it[AlquilerSalones.salonId],
                        nombre = it[AlquilerSalones.nombre],
                        telefono = it[AlquilerSalones.telefono],
                        fecha = it[AlquilerSalones.fecha].toKotlinLocalDateTime(),
                        observaciones = it[AlquilerSalones.observaciones],
                        monto = it[AlquilerSalones.monto],
                        condicion = it[AlquilerSalones.condicion],
                        metodoPagoId = it[AlquilerSalones.metodoPagoId],
                        dni = it[AlquilerSalones.dni]
                    )
                }
                .first()
        }
    }

    override fun findAll(): List<AlquilerSalon> {
        return transaction {
            AlquilerSalones.selectAll().map {
                AlquilerSalon(
                    alquilerId = it[AlquilerSalones.alquilerId],
                    salonId = it[AlquilerSalones.salonId],
                    nombre = it[AlquilerSalones.nombre],
                    telefono = it[AlquilerSalones.telefono],
                    fecha = it[AlquilerSalones.fecha].toKotlinLocalDateTime(),
                    observaciones = it[AlquilerSalones.observaciones],
                    monto = it[AlquilerSalones.monto],
                    condicion = it[AlquilerSalones.condicion],
                    metodoPagoId = it[AlquilerSalones.metodoPagoId],
                    dni = it[AlquilerSalones.dni]
                )
            }
        }
    }
    override fun actualizar(alquiler: AlquilerSalon) {
        transaction {
            AlquilerSalones.update({ AlquilerSalones.alquilerId eq alquiler.alquilerId!! }) {
                it[nombre] = alquiler.nombre
                it[salonId] = alquiler.salonId
                it[telefono] = alquiler.telefono
                it[fecha] = alquiler.fecha.toJavaLocalDateTime()
                it[observaciones] = alquiler.observaciones
                it[monto] = alquiler.monto
                it[condicion] = alquiler.condicion
                it[metodoPagoId] = alquiler.metodoPagoId
                it[dni] = alquiler.dni
            }
        }
    }
    override fun eliminarPorId(alquilerId: Int) {
        transaction {
            AlquilerSalones.deleteWhere { AlquilerSalones.alquilerId eq alquilerId }
        }
    }


}
