package com.example.application.commandhandler.cobrador

import com.example.application.command.cobrador.CreateCobradorCommand
import com.example.domain.contracts.ICobradorRepository
import com.example.domain.entities.Cobrador

class CreateCobradorHandler(
    private val cobradorRepository: ICobradorRepository
) {
    fun handle(command: CreateCobradorCommand) {
        try {
            // Validamos el comando
            command.validate()

            // Creamos el cobrador utilizando un método de fábrica
            val cobrador = Cobrador.create(
                command.nombre,
                command.telefono,
                command.dni,
                command.zona
            )

            // Guardamos el cobrador en la base de datos
            cobradorRepository.save(cobrador)

            println("✅Cobrador creado exitosamente")
        } catch (e: IllegalArgumentException) {
            //Error en la validación
            println("❌ Error en los datos proporcionados: ${e.message}")
        } catch (e: Exception) {
            // Cualquier otro error inesperado
            println("⚠️ Error inesperado al crear el cobrador: ${e.message}")
        }
    }
}
