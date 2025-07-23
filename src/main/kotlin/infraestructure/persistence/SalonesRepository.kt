package com.example.infraestructure.persistence

import com.example.domain.contracts.ISalonesRepository
import com.example.domain.entities.Pago
import com.example.domain.entities.Pagos
import com.example.domain.entities.Salon
import com.example.domain.entities.Salones
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


class SalonesRepository(private val database: Database) : ISalonesRepository {
    init {
        transaction(database) {
            SchemaUtils.create(Salones) // Crea la tabla si no existe
        }
    }

    // Guardar un pago en la base de datos
    override  fun save(salon: Salon): Salon {
        transaction {
            Salones.insert {
                it[nombre] = salon.nombre
                it[precio] = salon.precio
            }
        }
        return salon // 👈 Retornamos el objeto
    }
    override  fun findAll(): List<Salon>{
        return transaction {
            Salones.selectAll().map {
                Salon(
                    salonId = it[Salones.salonId],
                    nombre = it[Salones.nombre],
                    precio = it[Salones.precio]

                )
            }
        }

    }
    override fun findById(salonId: Int): Salon? {
        return transaction {
            Salones.select { Salones.salonId eq salonId }
                .map {
                    Salon(
                        salonId = it[Salones.salonId],
                        nombre = it[Salones.nombre],
                        precio = it[Salones.precio]
                    )
                }
                .singleOrNull()
        }
    }

    override fun actualizar(salon: Salon) {
        transaction {
            Salones.update({ Salones.salonId eq salon.salonId!! }) {
                it[nombre] = salon.nombre
                it[precio] = salon.precio
            }
        }
    }

    override fun delete(salonId: Int) {
        transaction {
            Salones.deleteWhere { Salones.salonId eq salonId }
        }
    }

}