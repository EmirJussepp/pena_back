package com.example.domain.Mappers

import com.example.domain.dto.SocioUpdateDTO
import com.example.domain.entities.Socio



fun mapearUpdateDTOaEntidad(dto: SocioUpdateDTO, socioExistente: Socio): Socio = Socio(
    socioId = dto.socioId,
    nombre = dto.nombre,
    apellido = dto.apellido,
    alias = dto.alias,
    numSocioBoca = dto.numSocioBoca,
    dni = dto.dni,
    estado = dto.estado,
    email = dto.email,
    telefono = dto.telefono ?: "",                      // nullable, OK
    cobradorId = dto.cobradorId,
    tipoSocioPeñaId = dto.tipoPenaId,            // atención al nombre exacto del campo
    tipoBocaId = dto.tipoBocaId,
    localidadId = dto.localidadId,
    userId = dto.userId,
    fechaInicio = socioExistente.fechaInicio,
    direccion = dto.direccion,
    fechaDeBaja= dto.fechaDeBaja
    )

