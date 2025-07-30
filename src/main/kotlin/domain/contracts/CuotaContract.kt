package com.example.domain.contracts



import com.example.domain.Dto.CuotaDTO
import com.example.domain.entities.Cuota

interface ICuotaRepository {
    fun save(cuota: Cuota)
    fun findAll(): List<Cuota>
    fun findBySocioId(socioId: Int): List<Cuota>
    fun marcarComoPagada(cuotaId: Int): Boolean
    fun findVencidas(): List<Cuota>
    fun findPendientesPorSocio(socioId: Int): List<Cuota>
    fun existeCuotaEnMes(socioId: Int, mes: Int, anio: Int): Boolean
    fun obtenerPorId(cuotaId: Int): Cuota?
    fun obtenerCuotaDeMes(socioId: Int, mes: Int, anio: Int): Cuota?
    fun obtenerUltimaCuotaDeSocio(socioId: Int): Cuota?
    fun obtenerCuotasVencidasPorCobrador(cobradorId: Int, mes: Int?, anio: Int?,
                                         dni: String?, page: Int,
                                         pageSize: Int): List<CuotaDTO>

}

