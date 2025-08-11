package com.example.application.commandhandler.ViajesPagos

import com.example.domain.contracts.ICobradorRepository
import com.example.domain.contracts.IMetodoPagoRepository
import com.example.domain.contracts.IViajesBombonera
import com.example.domain.contracts.IViajesPagosContract
import com.example.domain.entities.ViajePago
class ActualizarViajePagoHandler(
    private val viajePagoRepository: IViajesPagosContract,
//    private val metodoPagoRepository: IMetodoPagoRepository,
//    private val cobradorRepository: ICobradorRepository,
//    private val viajeRepository: IViajesBombonera
) {
    fun actualizarViajePago(viajePago: ViajePago): ViajePago {
        // Buscar el pago existente por viajePagoId
        val viajePagoActual = viajePagoRepository.findByViajeId(viajePago.viajePagoId!!)


        // Validaciones básicas
        if (viajePago.monto.toDouble() <= 0) throw IllegalArgumentException("Monto debe ser mayor a cero")
        if (viajePago.nombre.isNullOrBlank()) throw IllegalArgumentException("Nombre obligatorio")
        if (viajePago.apellido.isNullOrBlank()) throw IllegalArgumentException("Apellido obligatorio")


        // Actualizar y devolver
        return viajePagoRepository.update(viajePago)
    }
}

//
//        // Validar existencia de método de pago y cobrador
//        if (!metodoPagoRepository.existsById(viajePago.metodoPagoId)) {
//            throw IllegalArgumentException("Método de pago no válido")
//        }
//        if (!cobradorRepository.existsById(viajePago.cobradorId)) {
//            throw IllegalArgumentException("Cobrador no válido")
//        }
//        if (!viajeRepository.existsById(viajePago.viajeId)) {
//            throw IllegalArgumentException("Viaje no válido")
//        }
