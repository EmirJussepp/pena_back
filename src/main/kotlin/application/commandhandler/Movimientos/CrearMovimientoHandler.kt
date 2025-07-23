package com.example.application.commandhandler.Movimientos


import com.example.application.command.Movimientos.CrearMovimientoCommand

import com.example.domain.contracts.MovimientoContract

import com.example.domain.entities.Movimiento
import kotlinx.serialization.Serializable



@Serializable
class MovimientoCommandHandler(
    private val movimientoRepository: MovimientoContract
) {
    suspend fun handle(command: CrearMovimientoCommand): Movimiento {
        // Validar el comando
        command.validate()

        // Crear el nuevo movimiento usando el método de la entidad
        val movimiento = Movimiento.create(
            monto = command.monto,
            descripcion = command.descripcion,
            tipo = command.tipo,
            fecha = command.fecha,
            metodoPagoId = command.metodoPagoId
        )

        // Guardar el movimiento en el repositorio
        return movimientoRepository.save(movimiento)
    }
}
