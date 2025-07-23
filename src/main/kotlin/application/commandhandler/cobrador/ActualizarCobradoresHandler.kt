package com.example.application.commandhandler.cobrador

import com.example.application.command.cobrador.ActualizarCobrador
import com.example.infraestructure.persistence.CobradorRepository

class ActualizarCobradoresHandler(private val repository: CobradorRepository) {
    fun handle(id: Int, command: ActualizarCobrador) {
        val existente = repository.findById(id)
            ?: throw IllegalArgumentException("No existe cobrador con id $id")

        val actualizado = existente.copy(
            nombre = command.nombre,
            telefono = command.telefono,
            dni = command.dni,
            zona = command.zona
        )

        repository.actualizar(actualizado)
    }
}
