package com.example.application.commandhandler.tipoSocioBoca
import com.example.domain.contracts.ISocioBocaContract
import com.example.application.command.tipoSocioBoca.CreateSocioBocaCommand
import com.example.domain.entities.TipoSocioBoca

class CreateTipoSocioBocaCommandHandler(
    private val tipoSocioBocaRepository: ISocioBocaContract
) {
    fun handle(command: CreateSocioBocaCommand) {
        try {
            // Validamos el comando
            command.validate()

            // Creamos el tipo de socio boca y lo guardamos
            val tipoSocioBoca = TipoSocioBoca.create(
                command.nombre,
                command.precio
            )
            tipoSocioBocaRepository.save(tipoSocioBoca)

            println("✅ Tipo de socio boca creado exitosamente")

        } catch (e: IllegalArgumentException) {
            println("❌ Error: ${e.message}")
        } catch (e: Exception) {
            println("⚠️ Error inesperado al crear el tipo de socio boca: ${e.message}")
        }
    }
}
