package com.example.application.commandhandler.Salones

import com.example.application.command.Salones.CreateSalonCommand
import com.example.domain.contracts.ISalonesRepository
import com.example.domain.entities.Salon
import kotlinx.serialization.Serializable

@Serializable
class SalonCommandHandler(
    private val salonRepository: ISalonesRepository
) {
    suspend fun handle(command: CreateSalonCommand): Salon {
        // Validar el comando
        val errors = command.validate()
        if (errors.isNotEmpty()) {
            throw IllegalArgumentException(errors.joinToString(", "))
        }

        // Crear el salón usando la lógica de dominio
        val salon = Salon.create(
            nombre = command.nombre,
            precio = command.precio
        )

        // Guardar el salón
        return salonRepository.save(salon)
    }
}
