package com.example.domain.contracts

import com.example.domain.entities.ViajePago

interface IViajesPagosContract{
    fun save(viajePago: ViajePago): ViajePago
    fun findByViajeId(viajeId: Int): List<ViajePago>
    fun findAll(): List<ViajePago>
}