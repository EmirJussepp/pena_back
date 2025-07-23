package com.example.domain.contracts


import com.example.domain.entities.Salon

interface ISalonesRepository {
     fun save(salon: Salon) : Salon
     fun findAll(): List<Salon>
     fun findById(salonId: Int): Salon?
     fun actualizar(salon: Salon)
     fun delete(salonId: Int)
}