package com.example.infraestructure.persistence

import com.example.domain.dto.CuotaDTO
import com.example.domain.contracts.ICuotaRepository
import com.example.domain.entities.Beneficios

import com.example.domain.entities.Cuota
import com.example.domain.entities.Cuotas
import com.example.domain.entities.Socios
//import com.example.infraestructure.persistence.BeneficioRepository
import kotlinx.datetime.*

import java.time.LocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import org.jetbrains.exposed.sql.SortOrder

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq


import org.jetbrains.exposed.sql.javatime.month
import org.jetbrains.exposed.sql.javatime.year
import java.math.BigDecimal


class CuotaRepository(
    private val database: Database,
    private val beneficioRepository: BeneficioRepository
): ICuotaRepository {

    init {
        transaction(database) {
            SchemaUtils.create(Cuotas, Beneficios)
        }
    }
    override fun save(cuota: Cuota) {
        transaction {
            Cuotas.insert {
                it[socioId] = cuota.socioId
                it[monto] = cuota.monto
                it[estado] = cuota.estado
                it[fechaEmision] = cuota.fechaEmision?.toJavaLocalDateTime()
                it[fechaVencimiento] = cuota.fechaVencimiento.toJavaLocalDateTime()
            }
        }
    }



    override fun findAll(): List<Cuota> = transaction(database) {
        Cuotas.selectAll().map { rowToCuota(it) }
    }

    override fun findBySocioId(socioId: Int): List<Cuota> = transaction(database) {
        Cuotas.select { Cuotas.socioId eq socioId }
            .map { rowToCuota(it) }
    }

//    override fun marcarComoPagada(cuotaId: Int): Boolean = transaction(database) {
//        Cuotas.update({ Cuotas.cuotaId eq cuotaId }) {
//            it[estado] = true
//        } > 0
//    }
override fun marcarComoPagada(cuotaId: Int): Boolean = transaction(database) {
    // 1. Marcar la cuota como pagada
    val filasActualizadas = Cuotas.update({ Cuotas.cuotaId eq cuotaId }) {
        it[estado] = true
    }

    if (filasActualizadas == 0) return@transaction false

    // 2. Obtener el socioId de la cuota
    val cuota = Cuotas.select { Cuotas.cuotaId eq cuotaId }.singleOrNull()
    val socioId = cuota?.get(Cuotas.socioId) ?: return@transaction true

    // 3. Actualizar beneficio según condiciones actuales
    beneficioRepository.actualizarBeneficioSiCorresponde(socioId)

    true
}



    override fun findVencidas(): List<Cuota> = transaction(database) {
        val ahora = LocalDateTime.now()
        Cuotas.select {
            (Cuotas.fechaVencimiento less ahora) and (Cuotas.estado eq false)
        }.map { rowToCuota(it) }
    }

    override fun findPendientesPorSocio(socioId: Int): List<Cuota> = transaction(database) {
        Cuotas.select {
            (Cuotas.socioId eq socioId) and (Cuotas.estado eq false)
        }.map { rowToCuota(it) }
    }

override fun existeCuotaEnMes(socioId: Int, mes: Int, anio: Int): Boolean = transaction(database) {
    val fechaInicio = LocalDateTime.of(anio, mes, 1, 0, 0)
    val fechaFin = fechaInicio.plusMonths(1)

    Cuotas.select {
        (Cuotas.socioId eq socioId) and
                (Cuotas.fechaEmision greaterEq fechaInicio) and
                (Cuotas.fechaEmision less fechaFin)
//        Cuotas.select {
//            (Cuotas.socioId eq socioId) and
//                    (Cuotas.fechaVencimiento greaterEq fechaInicio) and
//                    (Cuotas.fechaVencimiento less fechaFin)
    }.count() > 0
}


    override fun obtenerPorId(cuotaId: Int): Cuota? = transaction(database) {
        Cuotas.select { Cuotas.cuotaId eq cuotaId }
            .mapNotNull { rowToCuota(it) }
            .singleOrNull()
    }

    override fun obtenerCuotaDeMes(socioId: Int, mes: Int, anio: Int): Cuota? = transaction(database) {
        val fechaInicio =LocalDateTime.of(anio, mes, 1, 0, 0)
        val fechaFin = fechaInicio.plusMonths(1)

        Cuotas.select {
            (Cuotas.socioId eq socioId) and
                    (Cuotas.fechaVencimiento greaterEq fechaInicio) and
                    (Cuotas.fechaVencimiento less fechaFin)
        }.mapNotNull { rowToCuota(it) }
            .singleOrNull()
    }

    override fun obtenerUltimaCuotaDeSocio(socioId: Int): Cuota? = transaction(database) {
        Cuotas.select { Cuotas.socioId eq socioId }
            .orderBy(Cuotas.fechaVencimiento, SortOrder.DESC)
            .limit(1)
            .map { rowToCuota(it) }
            .firstOrNull()
    }

override fun obtenerCuotasVencidasPorCobrador(
    cobradorId: Int,
    mes: Int?,
    anio: Int?,
    dni: String?,
    page: Int,
    pageSize: Int
): List<CuotaDTO> {
    return transaction {
//        var condicion = (Cuotas.estado eq false) and
//               (Cuotas.fechaVencimiento less LocalDateTime.now()) and
//                (Socios.cobradorId eq cobradorId)
        val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val inicioDeHoy = LocalDateTime(ahora.year, ahora.monthNumber, ahora.dayOfMonth, 0, 0)
        val inicioDeHoyJava = inicioDeHoy.toJavaLocalDateTime()

        var condicion = (Cuotas.estado eq false) and
                (Cuotas.fechaVencimiento lessEq inicioDeHoyJava) and
                (Socios.cobradorId eq cobradorId)
        if (anio != null) {
            condicion = condicion and (Cuotas.fechaVencimiento.year() eq anio)
        }

        if (mes != null) {
            condicion = condicion and (Cuotas.fechaVencimiento.month() eq mes)
        }

        if (!dni.isNullOrBlank()) {
            condicion = condicion and (Socios.dni eq dni)
        }

        println("⚙️ Filtros aplicados => cobradorId: $cobradorId, mes: $mes, año: $anio, dni: $dni")
        println("📜 Condición Exposed: $condicion")

        val resultados = (Cuotas innerJoin Socios)
            .select { condicion }
            .toList()

        println("🔢 Cantidad de cuotas encontradas: ${resultados.size}")

        resultados
            .drop((page - 1) * pageSize)
            .take(pageSize)
            .map {
                CuotaDTO(
                    cuotaId = it[Cuotas.cuotaId],
                    socioId = it[Socios.socioId],
                    nombreSocio = "${it[Socios.nombre]} ${it[Socios.apellido]}",
                    dni = it[Socios.dni],
                    monto = it[Cuotas.monto].toDouble(),
                    fechaVencimiento = it[Cuotas.fechaVencimiento],
                    estado = it[Cuotas.estado],
                    direccionSocio = "${it[Socios.direccion]}",
                    telefonoSocio = it[Socios.telefono]


                )
            }
    }

}
    override fun actualizarCuotasNoPagadas(tipoPeñaId: Int, nuevoMonto: BigDecimal) {
        transaction {
            Cuotas.update({
                (Cuotas.estado eq false) and
                        (Cuotas.socioId inSubQuery
                                Socios.slice(Socios.socioId)
                                    .select { Socios.tipoSocioPeñaId eq tipoPeñaId }
                                )
            }) {
                it[monto] = nuevoMonto
            }
        }
    }


    private fun rowToCuota(row: ResultRow): Cuota = Cuota(
        cuotaId = row[Cuotas.cuotaId],
        socioId = row[Cuotas.socioId],
        monto = row[Cuotas.monto],
        estado = row[Cuotas.estado],
        fechaEmision = row[Cuotas.fechaEmision]?.toKotlinLocalDateTime(),
        fechaVencimiento = row[Cuotas.fechaVencimiento].toKotlinLocalDateTime()
    )




}




