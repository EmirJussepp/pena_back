package com.example.domain.contracts



import com.example.domain.entities.Movimiento

import java.math.BigDecimal

import java.time.LocalDate
import java.time.YearMonth


interface MovimientoContract {
    suspend fun save(movimiento: Movimiento): Movimiento
    suspend fun findById(id: Int): Movimiento?
    suspend fun findAll(): List<Movimiento>
    suspend fun update(movimiento: Movimiento): Movimiento
//    suspend fun calcularBalance(): BigDecimal
//suspend fun calcularBalanceSemanal(desde: LocalDate, hasta: LocalDate): Pair<BigDecimal, List<Movimiento>>
    suspend fun eliminarPorId(movimientosId: Int): Boolean
//    suspend fun calcularBalanceMensual(mes: YearMonth): Pair<BigDecimal, List<Movimiento>>
suspend fun findMovimientosPorRango(desde: LocalDate, hasta: LocalDate): List<Movimiento>
    suspend fun findMovimientosPorMes(mes: YearMonth): List<Movimiento>

    suspend fun calcularBalance(): BigDecimal
    suspend fun calcularBalanceMensual(mes: YearMonth): Triple<BigDecimal, BigDecimal, BigDecimal>
    suspend fun calcularBalanceSemanal(desde: LocalDate, hasta: LocalDate): Triple<BigDecimal, BigDecimal, BigDecimal>
}