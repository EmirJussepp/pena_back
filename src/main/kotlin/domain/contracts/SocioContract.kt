package com.example.domain.contracts


import com.example.domain.entities.Socio
import com.example.domain.entities.SociosPage
import java.math.BigDecimal


interface ISocioRepository{
    fun save(socio:Socio):Socio
    fun findById(socioId: Int) : Socio?
    fun obtenerPaginadoYFiltrado(limit: Int, offset: Int, filtro: String?, estado: Boolean? = null): SociosPage
    fun obtenerSociosActivos(): List<Socio>
    fun findByName(nombre: String): List<Socio>
    fun existsByDni(dni: String): Boolean
    fun existsByNumSocioBoca(numSocioBoca: Int): Boolean
    fun existsByTelefono(telefono: String): Boolean
    fun existsByEmail(email: String): Boolean
    fun findByDni(dni: String): Socio?
   fun contarPorEstado(estado: Boolean): Int
//    fun obtenerCuotasVencidasPorCobrador(cobradorId: Int): List<CuotaDTO>
     fun update(socio: Socio): Socio
    fun obtenerPrecioTipoPeña(tipoPeñaId: Int): BigDecimal?
    fun actualizarCuotasPendientes(socioId: Int, nuevoMonto: BigDecimal)


}