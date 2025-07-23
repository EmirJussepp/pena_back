package com.example.domain.contracts

import com.example.domain.entities.AlquilerSalon

interface AlquilerSalonesContract {
    fun save(alquilerSalon: AlquilerSalon): AlquilerSalon
    fun findAll(): List<AlquilerSalon>
    fun actualizar(alquiler: AlquilerSalon)
    fun eliminarPorId(alquilerId: Int)
}
