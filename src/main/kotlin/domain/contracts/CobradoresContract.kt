package com.example.domain.contracts

import com.example.domain.entities.Cobrador

interface ICobradorRepository{
    fun save(cobrador: Cobrador)
    fun findById(cobradorId: Int) :Cobrador?
     fun obtenerTodos(): List<Cobrador>
    fun eliminarPorId(cobradoresId: Int)
    fun actualizar(cobrador: Cobrador)

}