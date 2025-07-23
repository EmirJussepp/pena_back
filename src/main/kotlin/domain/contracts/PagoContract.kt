package com.example.domain.contracts



import com.example.domain.entities.Pago

interface IPagoRepository {
     fun guardar(pago: Pago)
   fun findAll(): List<Pago>
    fun existePagoParaCuota(socioId: Int, cuotaId: Int): Boolean
    fun findById(pagoId: Int): Pago?
//    suspend fun findBySocioId(socioId: Int): List<Pago>
//    suspend fun tienePagoRegistrado(socioId: Int): Boolean
//    suspend fun marcarComoConfirmado(pagoId: Int): Boolean
//    suspend fun findPendientes(): List<Pago>
//    suspend fun findVencidos(): List<Pago>
}
