package com.example.infraestructure.persistence
import com.example.domain.entities.Movimiento
import com.example.domain.entities.Movimientos
import com.example.domain.contracts.MovimientoContract
import com.example.domain.entities.Cuotas
import com.example.domain.entities.Socios

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth


class MovimientoRepository(private val database: Database) : MovimientoContract {

    // Guardar o insertar un nuevo movimiento
    override suspend fun save(movimiento: Movimiento): Movimiento {
        return transaction(database) {
            // Insertar el movimiento y obtener el ID generado
            val insertedId = Movimientos.insert {
                it[fecha] = movimiento.fecha.toJavaLocalDateTime()
                it[tipo] = movimiento.tipo
                it[monto] = movimiento.monto
                it[descripcion] = movimiento.descripcion
                it[metodoPagoId] = movimiento.metodoPagoId
            }get Movimientos.movimientoId

            // Obtener el movimiento insertado con el ID generado
            Movimientos.select { Movimientos.movimientoId eq insertedId }
                .map {
                    Movimiento(
                        movimientoId = it[Movimientos.movimientoId],
                        fecha = it[Movimientos.fecha].toKotlinLocalDateTime(),
                        tipo = it[Movimientos.tipo],
                        monto = it[Movimientos.monto],
                        descripcion = it[Movimientos.descripcion],
                        metodoPagoId = it[Movimientos.metodoPagoId]
                    )
                }
                .first() // Se espera que solo haya un resultado
        }
    }

    // Obtener un movimiento por ID
    override suspend fun findById(id: Int): Movimiento? {
        return transaction(database) {
            Movimientos.select { Movimientos.movimientoId eq id }
                .map {
                    Movimiento(
                        movimientoId = it[Movimientos.movimientoId],
                        fecha = it[Movimientos.fecha].toKotlinLocalDateTime(),
                        tipo = it[Movimientos.tipo],
                        monto = it[Movimientos.monto],
                        descripcion = it[Movimientos.descripcion],
                        metodoPagoId = it[Movimientos.metodoPagoId]
                    )
                }
                .singleOrNull() // Devuelve el primer (y único) movimiento encontrado
        }
    }

    // Obtener todos los movimientos
    override suspend fun findAll(): List<Movimiento> {
        return transaction(database) {
            Movimientos.selectAll()
                .map {
                    Movimiento(
                        movimientoId = it[Movimientos.movimientoId],
                        fecha = it[Movimientos.fecha].toKotlinLocalDateTime(),
                        tipo = it[Movimientos.tipo],
                        monto = it[Movimientos.monto],
                        descripcion = it[Movimientos.descripcion],
                        metodoPagoId = it[Movimientos.metodoPagoId]
                    )
                }
        }
    }
    override suspend fun calcularBalance(): BigDecimal {
        val movimientos = findAll()
        val (total, _, _) = calcularBalancePorMetodoDesdeLista(movimientos)
        return total
    }

    override suspend fun calcularBalanceMensual(mes: YearMonth): Triple<BigDecimal, BigDecimal, BigDecimal> {
        val movimientos = findAll().filter {
            try {
                val fecha = it.fecha.toJavaLocalDateTime()
                YearMonth.from(fecha) == mes
            } catch (e: Exception) {
                println("❌ Error parseando fecha en movimiento ${it.movimientoId}: ${e.message}")
                false
            }
        }

        return calcularBalancePorMetodoDesdeLista(movimientos)
    }

    override suspend fun calcularBalanceSemanal(desde: LocalDate, hasta: LocalDate): Triple<BigDecimal, BigDecimal, BigDecimal> {
        val movimientos = findAll().filter {
            val fecha = it.fecha.toJavaLocalDateTime().toLocalDate()
            !fecha.isBefore(desde) && !fecha.isAfter(hasta)
        }

        return calcularBalancePorMetodoDesdeLista(movimientos)
    }

    private fun calcularBalancePorMetodoDesdeLista(movimientos: List<Movimiento>): Triple<BigDecimal, BigDecimal, BigDecimal> {
        var total = BigDecimal.ZERO
        var efectivo = BigDecimal.ZERO
        var transferencia = BigDecimal.ZERO

        for (m in movimientos) {
            val signo = when (m.tipo.lowercase()) {
                "ingreso" -> BigDecimal.ONE
                "egreso" -> BigDecimal.valueOf(-1)
                else -> {
                    println("⚠️ Tipo desconocido en movimiento ${m.movimientoId}: ${m.tipo}")
                    BigDecimal.ZERO
                }
            }

            val montoAjustado = signo * m.monto
            total += montoAjustado

            when (m.metodoPagoId) {
                1 -> efectivo += montoAjustado
                2 -> transferencia += montoAjustado
                else -> println("⚠️ Método de pago desconocido en movimiento ${m.movimientoId}: ${m.metodoPagoId}")
            }
        }

        return Triple(total, efectivo, transferencia)
    }


//    override suspend fun calcularBalance(): BigDecimal {
//        val movimientos = findAll() // o usar transaction si lo preferís
//        return movimientos.fold(BigDecimal.ZERO) { acc, movimiento ->
//            when (movimiento.tipo.lowercase()) {
//                "ingreso" -> acc + movimiento.monto
//                "egreso" -> acc - movimiento.monto
//                else -> acc
//            }
//        }
//    }
//
//    override suspend fun calcularBalanceMensual(mes: YearMonth): Pair<BigDecimal, List<Movimiento>> {
//        val movimientos = findAll().filter {
//            try {
//                val fecha = it.fecha.toJavaLocalDateTime()
//                YearMonth.from(fecha) == mes
//            } catch (e: Exception) {
//                println("❌ Error parseando fecha en movimiento ${it.movimientoId}: ${e.message}")
//                false
//            }
//        }
//
//        val balance = movimientos.fold(BigDecimal.ZERO) { acc, m ->
//            try {
//                when (m.tipo.lowercase()) {
//                    "ingreso" -> acc + m.monto
//                    "egreso" -> acc - m.monto
//                    else -> {
//                        println("⚠️ Tipo inválido en movimiento ${m.movimientoId}: ${m.tipo}")
//                        acc
//                    }
//                }
//            } catch (e: Exception) {
//                println("❌ Error calculando balance para movimiento ${m.movimientoId}: ${e.message}")
//                acc
//            }
//        }
//
//        return balance to movimientos
//    }
//
//    override suspend fun calcularBalanceSemanal(desde: LocalDate, hasta: LocalDate): Pair<BigDecimal, List<Movimiento>> {
//        val movimientos = findAll().filter {
//            val fecha = it.fecha.toJavaLocalDateTime().toLocalDate()
//            !fecha.isBefore(desde) && !fecha.isAfter(hasta)
//        }
//
//        val balance = movimientos.fold(BigDecimal.ZERO) { acc, m ->
//            when (m.tipo.lowercase()) {
//                "ingreso" -> acc + m.monto
//                "egreso" -> acc - m.monto
//                else -> acc
//            }
//        }
//
//        return balance to movimientos
//    }



    override suspend fun eliminarPorId(movimientosId: Int): Boolean {
        return transaction(database) {
            val deletedCount = Movimientos.deleteWhere { Movimientos.movimientoId eq movimientosId }
            deletedCount > 0
        }
    }

    override suspend fun findMovimientosPorRango(desde: LocalDate, hasta: LocalDate): List<Movimiento> {
        return findAll().filter {
            val fecha = it.fecha.toJavaLocalDateTime().toLocalDate()
            !fecha.isBefore(desde) && !fecha.isAfter(hasta)
        }
    }

    override suspend fun findMovimientosPorMes(mes: YearMonth): List<Movimiento> {
        return findAll().filter {
            val fecha = it.fecha.toJavaLocalDateTime()
            YearMonth.from(fecha) == mes
        }
    }





    // Actualizar un movimiento existente
    override suspend fun update(movimiento: Movimiento): Movimiento {
        return transaction(database) {
            Movimientos.update({ Movimientos.movimientoId eq movimiento.movimientoId!! }) {
                it[fecha] = movimiento.fecha.toJavaLocalDateTime()
                it[tipo] = movimiento.tipo
                it[monto] = movimiento.monto
                it[descripcion] = movimiento.descripcion
                it[metodoPagoId] = movimiento.metodoPagoId

            }

            // Retorna el movimiento actualizado
            movimiento
        }
    }
}