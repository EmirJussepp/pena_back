package com.example.application.commandhandler.ViajesBombonera

import com.example.application.command.ViajesBombonera.ActualizarViajeBombonera
import com.example.domain.entities.ViajeBombonera
import com.example.infraestructure.persistence.ViajeBomboneraRepository

class ActualizarViajeBomboneraHandler(
    private val viajeRepo: ViajeBomboneraRepository
) {
    fun handle(command: ActualizarViajeBombonera): ViajeBombonera {
        // Buscar primero para asegurarse de que existe
        val viajeExistente = viajeRepo.findById(command.viajeBomboneraId)
            ?: throw IllegalArgumentException("Viaje no encontrado")

        // Actualizar campos
        val viajeActualizado = viajeExistente.copy(
            fechaViaje = command.fechaViaje,
            destino = command.destino
        )

        return viajeRepo.update(viajeActualizado)
    }
}
