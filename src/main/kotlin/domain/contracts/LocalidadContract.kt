package com.example.domain.contracts

import com.example.domain.entities.Localidad


interface ILocalidadRepository {
    fun save(localidad: Localidad)

    fun findById(localidadId: Int): Localidad? // Agregar este método
    fun obtenerTodos(): List<Localidad>
//    fun findByName(name: String): List<Localidad>

}
