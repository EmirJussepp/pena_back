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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq



import java.math.BigDecimal
import java.time.Year


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
//
//    override fun obtenerCuotasVencidasPorCobrador(
//        cobradorId: Int,
//        mes: Int?,
//        anio: Int?,
//        dni: String?,
//        page: Int,
//        pageSize: Int
//    ): List<CuotaDTO> {
//        return transaction {
//            val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
//            val finDeHoy = LocalDateTime(ahora.year, ahora.monthNumber, ahora.dayOfMonth, 23, 59, 59)
//            val finDeHoyJava = finDeHoy.toJavaLocalDateTime()
//
//            // Condición base: cuotas impagas vencidas del cobrador
//            var condicion = (Cuotas.estado eq false) and
//                    (Cuotas.fechaVencimiento lessEq finDeHoyJava) and
//                    (Socios.cobradorId eq cobradorId)
//
//            // Filtro por mes y año usando rango de fechas
//            if (anio != null && mes != null) {
//                val fechaInicio = LocalDateTime(anio, mes, 1, 0, 0).toJavaLocalDateTime()
//                val fechaFin = LocalDateTime(
//                    anio,
//                    mes,
//                    Month.of(mes).length(Year.isLeap(anio.toLong())),
//                    23, 59, 59
//                ).toJavaLocalDateTime()
//                condicion = condicion and (Cuotas.fechaVencimiento.between(fechaInicio, fechaFin))
//            } else if (anio != null) {
//                val fechaInicio = LocalDateTime(anio, 1, 1, 0, 0).toJavaLocalDateTime()
//                val fechaFin = LocalDateTime(anio, 12, 31, 23, 59, 59).toJavaLocalDateTime()
//                condicion = condicion and (Cuotas.fechaVencimiento.between(fechaInicio, fechaFin))
//            } else if (mes != null) {
//                val fechaInicio = LocalDateTime(ahora.year, mes, 1, 0, 0).toJavaLocalDateTime()
//                val fechaFin = LocalDateTime(
//                    ahora.year,
//                    mes,
//                    Month.of(mes).length(Year.isLeap(ahora.year.toLong())),
//                    23, 59, 59
//                ).toJavaLocalDateTime()
//                condicion = condicion and (Cuotas.fechaVencimiento.between(fechaInicio, fechaFin))
//            }
//
//            // Filtro por DNI opcional
//            if (!dni.isNullOrBlank()) {
//                condicion = condicion and (Socios.dni eq dni)
//            }
//
//            println("⚙️ Filtros => cobradorId: $cobradorId, mes: $mes, año: $anio, dni: $dni")
//            println("📜 Condición Exposed: $condicion")
//
//            val offset = ((page - 1).coerceAtLeast(0) * pageSize).toLong()
//
//            // Base query con slice para no traer columnas innecesarias
//            val base = (Cuotas innerJoin Socios)
//                .slice(
//                    Cuotas.cuotaId,
//                    Socios.socioId,
//                    Socios.nombre,
//                    Socios.apellido,
//                    Socios.dni,
//                    Cuotas.monto,
//                    Cuotas.fechaVencimiento,
//                    Cuotas.estado,
//                    Socios.direccion,
//                    Socios.telefono
//                )
//                .select { condicion }
//
//            // (opcional) total para debug o paginación en el controlador/respuesta
//            val total = base.count()
//            println("🔢 Total cuotas (sin paginar): $total")
//
//            // Página actual desde la DB
//            val filas = base
//                .orderBy(Cuotas.fechaVencimiento to SortOrder.ASC)
//                .limit(pageSize, offset)
//                .toList()
//
//            filas.map {
//                CuotaDTO(
//                    cuotaId = it[Cuotas.cuotaId],
//                    socioId = it[Socios.socioId],
//                    nombreSocio = "${it[Socios.nombre]} ${it[Socios.apellido]}",
//                    dni = it[Socios.dni],
//                    monto = it[Cuotas.monto].toDouble(),
//                    fechaVencimiento = it[Cuotas.fechaVencimiento],
//                    estado = it[Cuotas.estado],
//                    direccionSocio = it[Socios.direccion] ?: "",
//                    telefonoSocio = it[Socios.telefono]
//                )
//            }
//        }
//    }
override fun obtenerCuotasVencidasPorCobrador(
    cobradorId: Int,
    mes: Int?,
    anio: Int?,
    dni: String?,
    page: Int,
    pageSize: Int
): List<CuotaDTO> {
    return transaction {

        // 🔹 Base: cuotas impagas del cobrador (SIN filtrar por hoy)
        var condicion = (Cuotas.estado eq false) and
                (Socios.cobradorId eq cobradorId)

        // 🔹 Filtro por mes/año (por PERÍODO)
        if (anio != null && mes != null) {
            val desde = LocalDateTime(anio, mes, 1, 0, 0).toJavaLocalDateTime()
            val hasta = LocalDateTime(
                anio,
                mes,
                Month.of(mes).length(Year.isLeap(anio.toLong())),
                23, 59, 59
            ).toJavaLocalDateTime()

            condicion = condicion and (Cuotas.fechaVencimiento.between(desde, hasta))

        } else if (anio != null) {
            val desde = LocalDateTime(anio, 1, 1, 0, 0).toJavaLocalDateTime()
            val hasta = LocalDateTime(anio, 12, 31, 23, 59, 59).toJavaLocalDateTime()
            condicion = condicion and (Cuotas.fechaVencimiento.between(desde, hasta))

        } else if (mes != null) {
            val anioActual = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).year

            val desde = LocalDateTime(anioActual, mes, 1, 0, 0).toJavaLocalDateTime()
            val hasta = LocalDateTime(
                anioActual,
                mes,
                Month.of(mes).length(Year.isLeap(anioActual.toLong())),
                23, 59, 59
            ).toJavaLocalDateTime()

            condicion = condicion and (Cuotas.fechaVencimiento.between(desde, hasta))
        }

        // 🔹 Filtro por DNI
        if (!dni.isNullOrBlank()) {
            condicion = condicion and (Socios.dni eq dni)
        }

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

        val filas = base
            .orderBy(Cuotas.fechaVencimiento to SortOrder.ASC)
            .limit(pageSize, offset)
            .toList()

        filas.map {
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




