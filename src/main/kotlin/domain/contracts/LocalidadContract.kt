package com.example.domain.contracts

import com.example.domain.entities.Localidad


interface ILocalidadRepository {
    fun save(localidad: Localidad)
    fun findById(localidadId: Int): Localidad?
    fun obtenerTodos(): List<Localidad>
    fun obtenerTodos(limit: Int): List<Localidad>
    fun buscar(q: String, limit: Int): List<Localidad>

    // NUEVO:
    fun upsertAndGetId(nombre: String, provincia: String, codigoPostal: String): Int
}

