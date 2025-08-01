package com.example.application.querysHandler



import com.example.domain.Dto.SocioDTO
import com.example.domain.entities.Socio
import com.example.domain.contracts.ISocioRepository
import com.example.domain.contracts.ICobradorRepository
import com.example.domain.contracts.ISocioPeñaContract
import com.example.domain.contracts.ISocioBocaContract
import com.example.domain.contracts.ILocalidadRepository
import kotlinx.serialization.Serializable

@Serializable
class GetSocioQueryHandler(
    private val socioRepository: ISocioRepository,
    private val cobradorRepository: ICobradorRepository,
    private val tipoSocioPeñaRepository: ISocioPeñaContract,
    private val tipoSocioBocaRepository: ISocioBocaContract,
    private val localidadRepository: ILocalidadRepository
) {
    // Método para convertir un Socio a SocioDTO
    fun toDto(socio: Socio): SocioDTO {
        val cobradorNombre = cobradorRepository.findById(socio.cobradorId)?.nombre ?: "Desconocido"
        val tipoPeñaNombre = tipoSocioPeñaRepository.findById(socio.tipoSocioPeñaId)?.nombre ?: "Desconocido"
        val tipoBocaNombre = socio.tipoBocaId?.let { tipoSocioBocaRepository.findById(it)?.nombre } ?: "-"
        val localidadNombre = localidadRepository.findById(socio.localidadId)?.nombre ?: "Desconocido"
        val fechaInicio = socio.fechaInicio

        return SocioDTO(
            socioId = socio.socioId ?: 0,
            nombre = socio.nombre,
            apellido = socio.apellido,
            dni = socio.dni,
            email = socio.email,
            telefono = socio.telefono,
            cobradorNombre = cobradorNombre,
            tipoPeñaNombre = tipoPeñaNombre,
            tipoBocaNombre = tipoBocaNombre,
            localidadNombre = localidadNombre,
            estado = socio.estado,
            alias = socio.alias,
            fechaInicio = fechaInicio,
            numSocioBoca = socio.numSocioBoca,
            direccion = socio.direccion?:"",
            fechaDeBaja = socio.fechaDeBaja
        )
    }

    // Método para manejar por ID
    fun handle(socioId: Int): SocioDTO {
        val socio = socioRepository.findById(socioId)
            ?: throw IllegalArgumentException("Socio con ID $socioId no encontrado")
        return toDto(socio)
    }
}

