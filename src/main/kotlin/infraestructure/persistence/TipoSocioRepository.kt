package com.example.infraestructure.persistence

import com.example.domain.contracts.ISocioBocaContract
import com.example.domain.entities.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class TipoSocioBocaRepository(private val database: Database) : ISocioBocaContract {

    init {
        transaction(database) {
            SchemaUtils.create(TiposSocioBoca) // Crea la tabla si no existe
        }
    }

    override fun save(tipoSocioBoca: TipoSocioBoca) {
        transaction(database) {
            val existingTipoSocioBoca =
                TiposSocioBoca.select { TiposSocioBoca.nombre eq tipoSocioBoca.nombre }.singleOrNull()

            if (existingTipoSocioBoca == null) {
                // Insertar un nuevo tipo de socio boca
                TiposSocioBoca.insert {
                    it[nombre] = tipoSocioBoca.nombre
                    it[precio] = tipoSocioBoca.precio
                }
            } else {
                // Actualizar tipo de socio boca existente
                TiposSocioBoca.update({ TiposSocioBoca.nombre eq tipoSocioBoca.nombre }) {
                    it[precio] = tipoSocioBoca.precio
                }
            }
        }
    }

    override fun findById(tipoSocioBocaId: Int): TipoSocioBoca? {
        return transaction(database) {
            TiposSocioBoca.select { TiposSocioBoca.tipoSocioBocaId eq tipoSocioBocaId }
                .map { row ->
                    TipoSocioBoca(
                        tipoSocioBocaId = row[TiposSocioBoca.tipoSocioBocaId],
                        nombre = row[TiposSocioBoca.nombre],
                        precio = row[TiposSocioBoca.precio]
                    )
                }
                .singleOrNull() // Retorna un solo tipo de Boca o null si no existe
        }
    }

    override fun getMontoById(tipoSocioBocaId: Int): Int? {
        return transaction {
            TiposSocioBoca.select { TiposSocioBoca.tipoSocioBocaId eq tipoSocioBocaId }
                .singleOrNull()?.get(TiposSocioBoca.precio)
        }
    }

    override fun obtenerTodos(): List<TipoSocioBoca> {
        return transaction(database) {
            TiposSocioBoca.selectAll().map {
                TipoSocioBoca(
                    tipoSocioBocaId = it[TiposSocioBoca.tipoSocioBocaId],
                    nombre = it[TiposSocioBoca.nombre],
                    precio = it[TiposSocioBoca.precio]  // <- sin coma aquí
                )
            }
        }
    }
    override fun actualizar(tipoSocioBoca: TipoSocioBoca) {
        transaction(database) {
            TiposSocioBoca.update({ TiposSocioBoca.tipoSocioBocaId eq tipoSocioBoca.tipoSocioBocaId!! }) {
                it[nombre] = tipoSocioBoca.nombre
                it[precio] = tipoSocioBoca.precio
            }
        }
    }
    override fun eliminar(tipoSocioBocaId: Int) {
        transaction(database) {
            val deletedCount = TiposSocioBoca.deleteWhere { TiposSocioBoca.tipoSocioBocaId eq tipoSocioBocaId }
            if (deletedCount == 0) {
                throw IllegalArgumentException("No se encontró tipo de socio con id $id")
            }
        }
    }


}

