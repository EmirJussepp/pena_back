package com.example.domain.contracts

import com.example.domain.entities.ViajeBombonera

interface IViajesBombonera{
    fun save(viajeBombonera: ViajeBombonera): ViajeBombonera
    fun findById(viajeBomboneraId: Int): ViajeBombonera?
    fun findAll(): List<ViajeBombonera>
    fun update(viajeBombonera: ViajeBombonera): ViajeBombonera
}


