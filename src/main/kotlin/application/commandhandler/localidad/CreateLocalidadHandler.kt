package com.example.application.commandhandler.localidad

import com.example.application.command.localidad.CreateLocalidadCommand
import com.example.domain.contracts.ILocalidadRepository
import com.example.domain.entities.Localidad

class CreateLocalidadHandler(
    private val localidadRepository: ILocalidadRepository
) {

    fun handle(command: CreateLocalidadCommand) {
        try {
            // Validamos el comando
            command.validate()

            // Creamos la localidad utilizando un método de fábrica
            val localidad = Localidad.create(
                command.nombre,
                command.provincia,
                command.codigoPostal
            )

            // Guardamos la localidad en la base de datos
            localidadRepository.save(localidad)

            println("✅ Localidad creada exitosamente")
        } catch (e: IllegalArgumentException) {
            // Error en la validación
            println("❌ Error en los datos proporcionados: ${e.message}")
        } catch (e: Exception) {
            // Cualquier otro error inesperado
            println("⚠️ Error inesperado al crear la localidad: ${e.message}")
        }
    }
}
