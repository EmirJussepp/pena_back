package com.example.application.commandhandler.AlquilerSalon

import com.example.application.command.AlquilerSalon.ActualizarAlquilerCommand
import com.example.domain.contracts.AlquilerSalonesContract
import com.example.domain.entities.AlquilerSalon
import java.time.LocalDate
import java.time.LocalDateTime

class ActualizarAlquilerSalonHandler(
    private val repo: AlquilerSalonesContract
) {
    fun handle(command: ActualizarAlquilerCommand) {
        command.validate()
        val fechaAlquiler = LocalDateTime.parse(command.fecha.toString())
        require(!fechaAlquiler.toLocalDate().isBefore(LocalDate.now())) {
            "No se puede registrar o editar un alquiler con fecha anterior a hoy."
        }
        val alquiler = AlquilerSalon(
            alquilerId = command.alquilerId,
            salonId = command.salonId,
            nombre = command.nombre,
            telefono = command.telefono,
            fecha = command.fecha,
            observaciones = command.observaciones,
            monto = command.monto,
            condicion = command.condicion,
            metodoPagoId = command.metodoPagoId,
            dni = command.dni
        )

        repo.actualizar(alquiler)
    }
}
