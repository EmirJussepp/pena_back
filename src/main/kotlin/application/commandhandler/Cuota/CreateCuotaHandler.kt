package com.example.application.commandhandler

import com.example.application.Service.CuotaService
import com.example.application.command.Cuota.CrearCuotaCommand
import com.example.domain.contracts.ICuotaRepository

import com.example.domain.contracts.ISocioRepository
import com.example.domain.entities.Cuota
import kotlinx.datetime.*

class CreateCuotaCommandHandler(
    private val cuotaRepository: ICuotaRepository,
    private val socioRepository: ISocioRepository,
    private val cuotaService: CuotaService
) {
    fun handle(command: CrearCuotaCommand) {
        try {
            // ✅ Validar campos del comando
            command.validate()

            // 🔍 Buscar socio
            val socio = socioRepository.findById(command.socioId)
                ?: throw IllegalArgumentException("Socio no encontrado con ID ${command.socioId}")

            // 💰 Calcular monto total
            val montoTotal = cuotaService.calcularMontoParaSocio(socio)

            // 📆 Obtener la fecha actual
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val fechaEmision = now

            // 📆 Calcular vencimiento para el día 4 del mes siguiente
            val fechaVencimiento = now.date
                .plus(1, DateTimeUnit.MONTH)
                .let { LocalDate(it.year, it.month, 4) }
                .atTime(0, 0)

            // 📆 Determinar mes y año a validar (de la fecha actual o del comando)
            val fechaBase = fechaVencimiento
            val mes = fechaBase.monthNumber // usamos monthNumber porque estás usando kotlinx.datetime.LocalDate
            val anio = fechaBase.year

            // ❌ Validar si ya existe cuota para ese mes/año
            if (cuotaRepository.existeCuotaEnMes(command.socioId, mes, anio)) {
                throw IllegalArgumentException("Ya existe una cuota para este socio en el mes $mes/$anio")
            }

            // 🧾 Crear cuota
            val cuota = Cuota.create(
                socioId = command.socioId,
                monto = montoTotal,
                fechaEmision = fechaEmision,
                fechaVencimiento = fechaVencimiento
            )

            // 💾 Guardar cuota
            cuotaRepository.save(cuota)

            println("✅ Cuota creada exitosamente para el socio ${command.socioId} con monto $montoTotal")

        } catch (e: IllegalArgumentException) {
            println("❌ Error: ${e.message}")
            throw e // importante relanzar para que la ruta pueda responder con BadRequest
        } catch (e: Exception) {
            println("⚠️ Error inesperado al crear la cuota: ${e.message}")
            throw e
        }
    }
}
