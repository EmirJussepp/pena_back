package com.example.application.commandhandler.tipoSocioBoca

import com.example.application.command.tipoSocioBoca.UpdateTipoSocioBocaCommand
import com.example.domain.entities.TipoSocioBoca
import com.example.infraestructure.persistence.TipoSocioBocaRepository

class UpdateTipoSocioBocaHandler(
    private val repository: TipoSocioBocaRepository
) {
    fun handle(command: UpdateTipoSocioBocaCommand) {
       command.validate()

        val tipoSocioBoca = TipoSocioBoca(
            tipoSocioBocaId = command.tipoSocioBocaId,
            nombre = command.nombre,
            precio = command.precio
            // agregar otros campos si los tienes
        )

        repository.actualizar(tipoSocioBoca)
    }
}
