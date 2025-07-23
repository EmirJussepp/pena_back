package com.example.application.commandhandler.SociosPeña



import com.example.application.command.SociosPeña.ActualizarPrecioPeñaCommand
import com.example.domain.entities.TipoSocioPeña
import com.example.infraestructure.persistence.SociosPeñaRepository
class ActualizarPrecioHandler(
    private val repository: SociosPeñaRepository
) {
    fun handle(command: ActualizarPrecioPeñaCommand) {
        command.validate()
        val tipoSocio = TipoSocioPeña(
            tipoSocioPeñaId = command.tipoSocioPeñaId,
            nombre = command.nombre,
            precio = command.precio
        )
        repository.actualizar(tipoSocio)
    }
}
