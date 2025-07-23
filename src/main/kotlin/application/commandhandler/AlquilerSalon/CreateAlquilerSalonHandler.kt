package com.example.application.commandhandler.AlquilerSalon


import com.example.application.command.AlquilerSalon.CreateAlquilerSalonCommand
import com.example.domain.contracts.AlquilerSalonesContract
import com.example.domain.entities.AlquilerSalon

import kotlinx.serialization.Serializable


@Serializable
class CreateAlquilerSalonHandler(
    private val alquilerRepository: AlquilerSalonesContract,

) {
     fun handle(command: CreateAlquilerSalonCommand): AlquilerSalon {
        // Validar el comando

        // Crear el alquiler usando la lógica de dominio
        val alquiler = AlquilerSalon.create(
            nombre = command.nombre,
            telefono = command.telefono,
            fecha = command.fecha,
            observaciones = command.observaciones,
            monto = command.monto,
            condicion = command.condicion,
            metodoPagoId = command.metodoPagoId,
            dni= command.dni,
            salonId = command.salonId
        )

        // Guardar el alquiler
        return alquilerRepository.save(alquiler)
    }
}
