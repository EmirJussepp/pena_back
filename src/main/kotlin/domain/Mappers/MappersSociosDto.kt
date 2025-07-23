package com.example.domain.Mappers

import com.example.domain.Dto.SocioDTO
import com.example.domain.contracts.ICobradorRepository
import com.example.domain.contracts.ILocalidadRepository
import com.example.domain.contracts.ISocioBocaContract
import com.example.domain.contracts.ISocioPeñaContract
import com.example.domain.entities.Socio


fun mapearSocioDTO(
    socio: Socio,
    cobradorRepo: ICobradorRepository,
    peñaRepo: ISocioPeñaContract,
    bocaRepo: ISocioBocaContract,
    localidadRepo: ILocalidadRepository
): SocioDTO {
    val cobrador = cobradorRepo.findById(socio.cobradorId)
    val tipoPeña = peñaRepo.findById(socio.tipoSocioPeñaId)
    val tipoBoca = bocaRepo.findById(socio.tipoBocaId)
    val localidad = localidadRepo.findById(socio.localidadId)

    return SocioDTO(
        socioId = socio.socioId ?: 0,
        nombre = socio.nombre,
        apellido = socio.apellido,
        fechaInicio = socio.fechaInicio,
        alias= socio.alias,
        numSocioBoca = socio.numSocioBoca,
        dni= socio.dni,
        estado = socio.estado,
        email = socio.email,
        telefono = socio.telefono,
        fechaDeBaja=socio.fechaDeBaja,
        direccion= socio.direccion?:"Desconocido",
        cobradorNombre = cobrador?.nombre ?: "Desconocido",
        tipoPeñaNombre = tipoPeña?.nombre ?: "Desconocido",
        tipoBocaNombre = tipoBoca?.nombre ?: "Desconocido",
        localidadNombre = localidad?.nombre ?: "Desconocido"
    )
}