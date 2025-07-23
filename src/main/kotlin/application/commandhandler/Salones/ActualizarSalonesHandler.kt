package com.example.application.commandhandler.Salones

import com.example.application.command.Salones.ActualzarSalones
import com.example.application.command.cobrador.ActualizarCobrador
import com.example.infraestructure.persistence.CobradorRepository
import com.example.infraestructure.persistence.SalonesRepository

class ActualizarSalonesHandler(private val repository: SalonesRepository) {
    fun handle(id: Int, command: ActualzarSalones) {
        val existente = repository.findById(id)
            ?: throw IllegalArgumentException("No existe cobrador con id $id")

        val actualizado = existente.copy(
            nombre = command.nombre,
            precio = command.precio
        )

        repository.actualizar(actualizado)
    }
}