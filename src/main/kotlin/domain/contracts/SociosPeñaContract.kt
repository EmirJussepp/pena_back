package com.example.domain.contracts


import com.example.domain.entities.TipoSocioPeña
import java.math.BigDecimal

interface ISocioPeñaContract {
    fun save(tipoSocioPeña: TipoSocioPeña)
    fun findById(tipoSocioPeñaId: Int): TipoSocioPeña?
    fun getMontoById(tipoSocioPeñaId: Int): Int?
//    fun updatePrice(nombre: String, nuevoPrecio: Int)
    fun obtenerTodos(): List<TipoSocioPeña>
    fun actualizar(tipo: TipoSocioPeña)
    fun eliminar(tipoSocioPeñaId: Int)
}
