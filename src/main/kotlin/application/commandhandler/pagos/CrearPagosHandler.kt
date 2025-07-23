package com.example.application.commandhandler.pagos

import com.example.application.Service.CuotaService
import com.example.application.command.pagos.CrearPagoCommand
import com.example.domain.contracts.ICuotaRepository
import com.example.domain.contracts.IPagoRepository
import com.example.domain.contracts.ISocioRepository
import com.example.domain.entities.Cuota
import com.example.domain.entities.Pago
import kotlinx.datetime.*

class CrearPagoCommandHandler(
    private val pagoRepository: IPagoRepository,
    private val cuotaRepository: ICuotaRepository,
    private val cuotaService: CuotaService,
    private val socioRepository: ISocioRepository
) {
    fun handle(command: CrearPagoCommand) {
        try {
            // ✅ Validar campos del comando
            val errores = command.validate()
            if (errores.isNotEmpty()) throw IllegalArgumentException(errores.joinToString(" | "))

            val socio = socioRepository.findById(command.socioId)
                ?: throw IllegalArgumentException("Socio no encontrado")

            val monto = cuotaService.calcularMontoParaSocio(socio)

            for (cuotaId in command.cuotaId) {
                val cuota = cuotaRepository.obtenerPorId(cuotaId)
                    ?: throw IllegalArgumentException("Cuota no encontrada con ID $cuotaId")

                if (cuota.socioId != command.socioId)
                    throw IllegalArgumentException("La cuota $cuotaId no pertenece al socio.")

                if (pagoRepository.existePagoParaCuota(command.socioId, cuotaId))
                    throw IllegalArgumentException("El socio ya ha pagado la cuota $cuotaId.")

                // 💳 Registrar el pago
                val pago = Pago.create(
                    socioId = command.socioId,
                    cuotaId = cuotaId,
                    monto = monto,
                    metodoPagoId = command.metodoPagoId
                )

                pagoRepository.guardar(pago)
                cuotaRepository.marcarComoPagada(cuotaId)

                println("✅ Pago registrado para cuota $cuotaId")
            }

        } catch (e: IllegalArgumentException) {
            println("❌ Error de validación: ${e.message}")
            throw e
        } catch (e: Exception) {
            println("⚠️ Error inesperado al crear el pago: ${e.message}")
            throw e
        }
    }
}


