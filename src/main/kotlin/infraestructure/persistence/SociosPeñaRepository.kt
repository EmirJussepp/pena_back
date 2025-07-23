package com.example.infraestructure.persistence

import com.example.domain.contracts.ISocioPeñaContract
import com.example.domain.entities.TipoSocioBoca
import com.example.domain.entities.TiposSocioPeña
import com.example.domain.entities.TipoSocioPeña
import com.example.domain.entities.TiposSocioBoca
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction


class SociosPeñaRepository(private val database: Database) : ISocioPeñaContract {

    init {
        transaction(database) {
            SchemaUtils.create(TiposSocioPeña) // Crea la tabla si no existe
        }
    }

    override fun save(tipoSocioPeña: TipoSocioPeña) {
        transaction(database) {
            val existingTipoSocioPeña = TiposSocioPeña
                .select { TiposSocioPeña.nombre eq tipoSocioPeña.nombre }
                .singleOrNull()

            if (existingTipoSocioPeña == null) {
                // Insertar un nuevo tipo de socio peña
                TiposSocioPeña.insert {
                    it[nombre] = tipoSocioPeña.nombre
                    it[precio] = tipoSocioPeña.precio
                }
            } else {
                // Actualizar tipo de socio peña existente
                TiposSocioPeña.update({ TiposSocioPeña.nombre eq tipoSocioPeña.nombre }) {
                    it[precio] = tipoSocioPeña.precio
                }
            }
        }
    }
    override fun findById(tipoSocioPeñaId: Int): TipoSocioPeña? {
        return transaction(database) {
            TiposSocioPeña.select { TiposSocioPeña.tipoSocioPeñaId eq tipoSocioPeñaId }
                .map { row ->
                    TipoSocioPeña(
                        tipoSocioPeñaId = row[TiposSocioPeña.tipoSocioPeñaId],
                        nombre = row[TiposSocioPeña.nombre],
                        precio = row[TiposSocioPeña.precio]
                    )
                }
                .singleOrNull() // Retorna un solo tipo de socio Peña o null si no existe
        }
    }
    override fun getMontoById(tipoSocioPeñaId: Int): Int {
        return transaction {
            TiposSocioPeña.select { TiposSocioPeña.tipoSocioPeñaId eq tipoSocioPeñaId }
                .singleOrNull()?.get(TiposSocioPeña.precio)
                ?: throw IllegalArgumentException("Tipo Socio Peña no encontrado con ID $tipoSocioPeñaId")
        }
    }
    override fun actualizar(tipo: TipoSocioPeña) {
        transaction(database) {
            TiposSocioPeña.update({ TiposSocioPeña.tipoSocioPeñaId eq tipo.tipoSocioPeñaId!! }) {
                it[nombre] = tipo.nombre
                it[precio] = tipo.precio
            }
        }
    }

    override fun obtenerTodos(): List<TipoSocioPeña> {
        return transaction(database) {
            TiposSocioPeña.selectAll().map {
                TipoSocioPeña(
                    tipoSocioPeñaId = it[TiposSocioPeña.tipoSocioPeñaId],
                    nombre = it[TiposSocioPeña.nombre],
                    precio = it[TiposSocioPeña.precio]
                )
            }
        }
    }
   override fun eliminar(tipoSocioPeñaId: Int) {
        transaction(database) {
            val deletedCount = TiposSocioPeña.deleteWhere { TiposSocioPeña.tipoSocioPeñaId eq tipoSocioPeñaId }
            if (deletedCount == 0) {
                throw IllegalArgumentException("No se encontró tipo de socio con id $id")
            }
        }
    }



}