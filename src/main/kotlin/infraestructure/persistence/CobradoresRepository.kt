package com.example.infraestructure.persistence


import com.example.domain.contracts.ICobradorRepository
import com.example.domain.entities.Cobrador
import com.example.domain.entities.Cobradores

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction


class CobradorRepository(private val database: Database) : ICobradorRepository {

    override fun save(cobrador: Cobrador) {
        transaction(database) {
            if (cobrador.cobradoresId == null) {
                // Insertar un nuevo cobrador
                Cobradores.insert {
                    it[nombre] = cobrador.nombre
                    it[telefono] = cobrador.telefono
                    it[dni] = cobrador.dni
                    it[zona] = cobrador.zona
                }
            } else {
                // Actualizar un cobrador existente
                Cobradores.update({ Cobradores.cobradoresId eq cobrador.cobradoresId }) {
                    it[nombre] = cobrador.nombre
                    it[telefono] = cobrador.telefono
                    it[dni] = cobrador.dni
                    it[zona] = cobrador.zona
                }
            }
        }
    }

    override fun findById(cobradorId: Int): Cobrador? {
        return transaction(database) {
            Cobradores.select { Cobradores.cobradoresId eq cobradorId }
                .map {
                    Cobrador(
                        cobradoresId = it[Cobradores.cobradoresId],
                        nombre = it[Cobradores.nombre],
                        dni = it[Cobradores.dni],
                        telefono = it[Cobradores.telefono],
                        zona = it[Cobradores.zona]
                    )
                }
                .singleOrNull()
        }
    }

    override fun obtenerTodos(): List<Cobrador> {
        return transaction(database) {
            Cobradores.selectAll().map {
                Cobrador(
                    cobradoresId = it[Cobradores.cobradoresId],
                    nombre = it[Cobradores.nombre],
                    dni = it[Cobradores.dni],
                    telefono = it[Cobradores.telefono],
                    zona = it[Cobradores.zona]

                )
            }
        }
    }
    override fun eliminarPorId(cobradoresId: Int) {
        transaction(database) {
            Cobradores.deleteWhere { Cobradores.cobradoresId eq cobradoresId }
        }
    }
    override fun actualizar(cobrador: Cobrador) {
        transaction(database) {
            Cobradores.update({ Cobradores.cobradoresId eq cobrador.cobradoresId!! }) {
                it[nombre] = cobrador.nombre
                it[telefono] = cobrador.telefono
                it[dni] = cobrador.dni
                it[zona] = cobrador.zona
            }
        }
    }


//
//    override fun findAll(): List<Cobrador> {
//        return transaction(database) {
//            Cobradores.selectAll().map {
//                Cobrador(
//                    cobradoresId = it[Cobradores.cobradoresId],
//                    nombre = it[Cobradores.nombre],
//                    dni = it[Cobradores.dni],
//                    zona = it[Cobradores.zona]
//                )
//            }
//        }
//    }
//
//    override fun delete(cobradorId: Int) {
//        transaction(database) {
//            Cobradores.deleteWhere { cobradoresId eq cobradorId }
//        }
//    }
}