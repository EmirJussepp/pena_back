package com.example.application.commandhandler.ViajesPagos



import com.example.application.command.ViajesPagos.CreateViajesPagosCommand
import com.example.domain.contracts.IViajesPagosContract
import com.example.domain.entities.ViajePago

import kotlinx.datetime.toKotlinLocalDateTime

import java.math.BigDecimal
import java.time.LocalDateTime


class ViajePagoCommandHandler(private val viajePagoRepository: IViajesPagosContract) {

    fun handle(command: CreateViajesPagosCommand): ViajePago {
        // Validación del comando
        command.validate()

        // Creación de la entidad ViajePago
        val viajePago = ViajePago(
            viajePagoId = null, // Se genera en la BD
            viajeId = command.viajeId,
            monto = BigDecimal(command.monto),
            nombre = command.nombre,
            apellido = command.apellido,
            dni = command.dni,

            metodoPagoId = command.metodoPagoId,
        )

        // Guardar en el repositorio
        return viajePagoRepository.save(viajePago)
    }
}