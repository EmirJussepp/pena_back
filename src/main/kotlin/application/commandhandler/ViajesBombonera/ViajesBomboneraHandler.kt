package com.example.application.commandhandler
import com.example.domain.contracts.IViajesBombonera
import com.example.application.command.ViajesBombonera.ViajeBomboneraCommand
import com.example.domain.entities.ViajeBombonera
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
class ViajeBomboneraCommandHandler(
    private val viajeBomboneraRepository: IViajesBombonera
) {
    fun handle(command: ViajeBomboneraCommand) {
        try {
            // Validamos el comando
            command.validate()


            // Creamos el objeto ViajeBombonera
            val viajeBombonera = ViajeBombonera.create(
                fechaViaje= command.fechaViaje,  // Ahora pasamos un LocalDateTime válido
                destino= command.destino,
            )

            // Guardamos en el repositorio
            viajeBomboneraRepository.save(viajeBombonera)

            println("✅ Viaje creado o actualizado exitosamente")

        } catch (e: IllegalArgumentException) {
            println("❌ Error: ${e.message}")
        } catch (e: Exception) {
            println("⚠️ Error inesperado al crear o actualizar el viaje: ${e.message}")
        }
    }
}


