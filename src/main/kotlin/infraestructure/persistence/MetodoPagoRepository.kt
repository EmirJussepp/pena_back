package com.example.infraestructure.persistence



import com.example.domain.entities.metodoPago
import com.example.domain.entities.metodosPago
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import com.example.domain.contracts.IMetodoPagoRepository
class MetodoPagoRepository(private val database: Database) : IMetodoPagoRepository {

    init {
        transaction(database) {
            SchemaUtils.create(metodosPago) // Crea la tabla si no existe
        }
    }

    override fun save(metodoPago: metodoPago) {
        transaction(database) {
            val existingMetodo = metodosPago.select { metodosPago.nombre eq metodoPago.nombre }.singleOrNull()

            if (existingMetodo == null) {
                // Insertar un nuevo método de pago
                metodosPago.insert {
                    it[nombre] = metodoPago.nombre
                }
            } else {
                // Actualizar el método de pago existente
                metodosPago.update({ metodosPago.nombre eq metodoPago.nombre }) {
                    it[nombre] = metodoPago.nombre
                }
            }
        }
    }

    fun findAll(): List<metodoPago> = transaction(database) {
        metodosPago.selectAll().map {
            metodoPago(
                metodoPagoId = it[metodosPago.metodoPagoId],
                nombre = it[metodosPago.nombre]
            )
        }
    }

//    fun findById(id: Int): metodoPago? = transaction(database) {
//        metodosPago.select { metodosPago.metodoPagoId eq id }
//            .map {
//                metodoPago(
//                    metodoPagoId = it[metodosPago.metodoPagoId],
//                    nombre = it[metodosPago.nombre]
//                )
//            }
//            .singleOrNull()
//    }

//    fun delete(id: Int) {
//        transaction(database) {
//            metodosPago.deleteWhere { metodosPago.metodoPagoId eq id }
//        }
//    }
}
