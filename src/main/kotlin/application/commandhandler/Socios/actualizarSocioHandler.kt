package com.example.application.commandhandler.Socios


import com.example.domain.contracts.ISocioRepository
import com.example.domain.entities.Socio


class actualizarSocioHandler(
    private val socioRepository: ISocioRepository
) {
    fun actualizarSocio(socio: Socio): Socio {
        val socioActual = socioRepository.findById(socio.socioId!!)
            ?: throw IllegalArgumentException("Socio no encontrado")

        val tipoAnterior = socioActual.tipoSocioPeñaId
        val tipoNuevo = socio.tipoSocioPeñaId

        // Si el tipo de socio cambió
        if (tipoAnterior != tipoNuevo) {
            val nuevoMonto = socioRepository.obtenerPrecioTipoPeña(tipoNuevo)
                ?: throw IllegalArgumentException("Tipo de socio Peña no encontrado")

            socioRepository.actualizarCuotasPendientes(socio.socioId!!, nuevoMonto)
        }

        return socioRepository.update(socio)
    }
}