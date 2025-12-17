package com.example.application.Service

import com.example.domain.contracts.ICuotaRepository

import com.example.domain.contracts.ISocioPeñaContract
import com.example.domain.contracts.ISocioRepository
import com.example.domain.entities.Cuota

import com.example.domain.entities.Socio

import com.example.infraestructure.persistence.BeneficioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*



import java.math.BigDecimal

import kotlin.time.Duration.Companion.days


class CuotaService(
    private val tipoSocioPeñaRepository: ISocioPeñaContract,
    private val cuotaRepository: ICuotaRepository,
    private val socioRepository: ISocioRepository,
    private val beneficioRepository: BeneficioRepository
) {

    fun calcularMontoParaSocio(socio: Socio): BigDecimal {
        return socio.calcularMontoCuota(
            getMontoPeña = { tipoSocioPeñaRepository.getMontoById(it)?.toDouble() },

        )
    }




    fun generarCuotaParaNuevoSocio(socio: Socio) {
        val ahora = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val mesActual = ahora.monthNumber
        val anioActual = ahora.year

        if (cuotaRepository.existeCuotaEnMes(socio.socioId!!, mesActual, anioActual)) {
            println("⚠️ Ya existe una cuota para el socio ${socio.socioId} en $mesActual/$anioActual")
            return
        }

        val monto = calcularMontoParaSocio(socio)
        val fechaEmision = ahora
        val fechaVencimiento = LocalDate(ahora.year, ahora.month, 1).atTime(0, 0)

        val cuota = Cuota.create(
            socioId = socio.socioId,
            monto = monto,
            fechaEmision = fechaEmision,
            fechaVencimiento = fechaVencimiento
        )

        cuotaRepository.save(cuota)
        beneficioRepository.actualizarBeneficioSiCorresponde(socio.socioId)
        println("✅ Cuota inicial generada para el socio ${socio.socioId} - $mesActual/$anioActual")
    }
//    fun generarCuotasMensuales() {
//        val sociosActivos = socioRepository.obtenerSociosActivos()
//
//        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
//        val fechaEmision = now
//
//        val fechaVencimiento = now.date
//            .plus(1, DateTimeUnit.MONTH)
//            .let { LocalDate(it.year, it.month, 1) }
//            .atTime(0, 0)
//
//        val mes = fechaEmision.monthNumber
//        val anio = fechaEmision.year
//
//
//        for (socio in sociosActivos) {
//            if (!cuotaRepository.existeCuotaEnMes(socio.socioId!!, mes, anio)) {
//                crearNuevaCuota(socio, fechaEmision, fechaVencimiento)
//            }
//        }
//    }
fun generarCuotasMensuales() {
    val sociosActivos = socioRepository.obtenerSociosActivos()

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val fechaEmision = now

    // 👉 La cuota siempre vence el 1 del mes siguiente
    val fechaVencimiento = now.date
        .plus(1, DateTimeUnit.MONTH)
        .let { LocalDate(it.year, it.month, 1) }
        .atTime(0, 0)

    // ✅ USAR EL PERÍODO DEL VENCIMIENTO
    val mes = fechaVencimiento.monthNumber
    val anio = fechaVencimiento.year

    println("🧾 Generando cuotas para período $mes/$anio (venc: $fechaVencimiento)")

    for (socio in sociosActivos) {
        val socioId = socio.socioId ?: continue

        if (!cuotaRepository.existeCuotaEnMes(socioId, mes, anio)) {
            crearNuevaCuota(socio, fechaEmision, fechaVencimiento)
        }
    }
}



    private fun crearNuevaCuota(socio: Socio, fechaEmision: LocalDateTime, fechaVencimiento: LocalDateTime) {
        val montoTotal = calcularMontoParaSocio(socio)

        val cuota = Cuota.create(
            socioId = socio.socioId!!,
            monto = montoTotal,
            fechaEmision = fechaEmision,
            fechaVencimiento = fechaVencimiento
        )

        cuotaRepository.save(cuota)
        beneficioRepository.actualizarBeneficioSiCorresponde(socio.socioId)
        println("✅ Cuota generada para el socio ${socio.socioId} con monto $montoTotal")
    }

    fun generarCuotasDesdeFecha(socioId: Int, fechaDeBaja: LocalDateTime) {
        val socio = socioRepository.findById(socioId) ?: return

        // Convertir LocalDateTime a LocalDate y sumar un mes:
        var fecha = fechaDeBaja.date.plus(1, DateTimeUnit.MONTH).atTime(0, 0)

        val hoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        while (fecha <= hoy) {
            val mes = fecha.monthNumber
            val anio = fecha.year

            if (!cuotaRepository.existeCuotaEnMes(socioId, mes, anio)) {
                val monto = calcularMontoParaSocio(socio)
                val fechaVencimiento = fecha.date.let {
                    LocalDate(it.year, it.month, 1)
                }.atTime(0, 0)

                val cuota = Cuota.create(
                    socioId = socioId,
                    monto = monto,
                    fechaEmision = fecha,
                    fechaVencimiento = fechaVencimiento
                )

                cuotaRepository.save(cuota)
                println("✅ Cuota generada al reactivar socio $socioId para $mes/$anio")
            }

            // Sumás un mes para siguiente iteración:
            fecha = fecha.date.plus(1, DateTimeUnit.MONTH).atTime(0, 0)
        }
        beneficioRepository.actualizarBeneficioSiCorresponde(socioId)

    }

    fun iniciarSchedulerCuotasMensuales() {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                println("🕐 Ejecutando scheduler a las $now")
                generarCuotasMensuales()
                delay(1.days)
            }
        }
    }
}


