package com.example.infraestructure.persistence

import com.example.domain.dto.CuotaDTO
import com.example.domain.contracts.ICuotaRepository
import com.example.domain.entities.Beneficios

import com.example.domain.entities.Cuota
import com.example.domain.entities.Cuotas
import com.example.domain.entities.Socios

import kotlinx.datetime.*

import java.time.LocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import org.jetbrains.exposed.sql.SortOrder


import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import java.time.LocalDateTime as JLocalDateTime





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
        val inicio = LocalDateTime.of(anio, mes, 1, 0, 0)
        val fin = inicio.plusMonths(1)

        Cuotas.select {
            (Cuotas.socioId eq socioId) and
                    (Cuotas.fechaVencimiento greaterEq inicio) and
                    (Cuotas.fechaVencimiento less fin)
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
            val ahoraKx = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val finDeHoyJava = JLocalDateTime.of(
                ahoraKx.year, ahoraKx.monthNumber, ahoraKx.dayOfMonth, 23, 59, 59
            )

            // ✅ BASE: pendientes (0) del cobrador
            var condicion = (Cuotas.estado eq false) and (Socios.cobradorId eq cobradorId)


            // ✅ Período (mes/año) => vencimiento mes siguiente
            fun rangoVencimientoPorPeriodo(m: Int, a: Int): Pair<JLocalDateTime, JLocalDateTime> {
                val inicioVenc = JLocalDateTime.of(a, m, 1, 0, 0).plusMonths(1) // mes siguiente
                val finVenc = JLocalDateTime.of(a, m, 1, 0, 0).plusMonths(2)   // fin exclusivo
                return inicioVenc to finVenc
            }

            if (anio != null && mes != null) {
                val (inicio, fin) = rangoVencimientoPorPeriodo(mes, anio)
                condicion = condicion and
                        (Cuotas.fechaVencimiento greaterEq inicio) and
                        (Cuotas.fechaVencimiento less fin)

            } else if (anio != null) {
                // Año completo del período -> vencimientos: feb(anio) .. feb(anio+1)
                val inicio = JLocalDateTime.of(anio, 1, 1, 0, 0).plusMonths(1)
                val fin = JLocalDateTime.of(anio + 1, 1, 1, 0, 0).plusMonths(1)

                condicion = condicion and
                        (Cuotas.fechaVencimiento greaterEq inicio) and
                        (Cuotas.fechaVencimiento less fin)

            } else if (mes != null) {
                val (inicio, fin) = rangoVencimientoPorPeriodo(mes, ahoraKx.year)
                condicion = condicion and
                        (Cuotas.fechaVencimiento greaterEq inicio) and
                        (Cuotas.fechaVencimiento less fin)
            }

            // DNI opcional
            if (!dni.isNullOrBlank()) {
                condicion = condicion and (Socios.dni eq dni)
            }

            // 🔁 Opcional: si querés SOLO vencidas, descomentá:
            // condicion = condicion and (Cuotas.fechaVencimiento lessEq finDeHoyJava)

            val offset = ((page - 1).coerceAtLeast(0) * pageSize).toLong()

            val base = (Cuotas innerJoin Socios)
                .slice(
                    Cuotas.cuotaId,
                    Socios.socioId,
                    Socios.nombre,
                    Socios.apellido,
                    Socios.dni,
                    Cuotas.monto,
                    Cuotas.fechaVencimiento,
                    Cuotas.estado,
                    Socios.direccion,
                    Socios.telefono
                )
                .select { condicion }

            base
                .orderBy(Cuotas.fechaVencimiento to SortOrder.ASC)
                .limit(pageSize, offset)
                .map {
                    CuotaDTO(
                        cuotaId = it[Cuotas.cuotaId],
                        socioId = it[Socios.socioId],
                        nombreSocio = "${it[Socios.nombre]} ${it[Socios.apellido]}",
                        dni = it[Socios.dni],
                        monto = it[Cuotas.monto].toDouble(),
                        fechaVencimiento = it[Cuotas.fechaVencimiento],
                        estado = it[Cuotas.estado],
                        direccionSocio = it[Socios.direccion] ?: "",
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




