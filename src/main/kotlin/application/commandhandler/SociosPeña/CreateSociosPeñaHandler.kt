package com.example.application.commandhandler.SociosPeña

import com.example.application.command.SociosPeña.CreateSociosPeñaCommand
import com.example.infraestructure.persistence.SociosPeñaRepository
import com.example.domain.entities.TipoSocioPeña
class CreateSociosPeñaCommandHandler(  // ✅ Nombre corregido
    private val sociosPeñaRepository: SociosPeñaRepository  // ✅ Nombre coherente
) {
    fun handle(command: CreateSociosPeñaCommand) {
        try {
            // Validamos el comando
            command.validate()

            // Creamos un nuevo SocioPeña y lo guardamos en la base de datos
            val nuevoSocioPeña = TipoSocioPeña.create(  // ✅ Llamada corregida
                command.nombre,
                command.precio
            )

            // Guardamos el nuevo socio en la base de datos
            sociosPeñaRepository.save(nuevoSocioPeña)

            println("✅ Socio de Peña creado exitosamente")

        } catch (e: IllegalArgumentException) {
            println("❌ Error: ${e.message}")
            throw e  // ✅ Se relanza la excepción para manejarla en la ruta
        } catch (e: Exception) {
            println("⚠️ Error inesperado al crear el SocioPeña: ${e.message}")
            throw e  // ✅ Se relanza la excepción para depuración adecuada
        }
    }
}
