package com.example.application.querysHandler
import com.example.application.querys.ObtenerSocioIdQuery
import com.example.application.querys.ObtenerSocioPorNombreQuery
import com.example.domain.entities.Socio
import com.example.domain.contracts.ISocioRepository
// Manejador para obtener un Socio por su ID
class ObtenerSocioIdHandler(private val socioRepository: ISocioRepository) {
    fun handle(query: ObtenerSocioIdQuery): Socio? {
        return socioRepository.findById(query.socioId)
    }
}

// Manejador para obtener Socios por su nombre
class ObtenerSocioNombreHandler(private val socioRepository: ISocioRepository) {
    fun handle(query: ObtenerSocioPorNombreQuery): List<Socio> {
        return socioRepository.findByName(query.nombre)
    }
}
