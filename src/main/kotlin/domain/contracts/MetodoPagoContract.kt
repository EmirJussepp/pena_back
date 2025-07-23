package com.example.domain.contracts

import com.example.domain.entities.metodoPago

interface IMetodoPagoRepository{
    fun save(metodoPago: metodoPago)

}