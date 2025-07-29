package com.example.infraestructure.persistence

import com.example.domain.Dto.BeneficioConSocioDTO
import com.example.domain.Dto.PaginadoBeneficiosResponse
import com.example.domain.entities.Beneficios
import com.example.domain.entities.Socios

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
  // ✅ asegúrate que apunta al archivo correcto

class BeneficioRepository(
    private val database: Database
) {
    fun obtenerBeneficiosPaginadoYFiltrado(
        pagina: Int,
        limite: Int,
        filtro: String?
    ): PaginadoBeneficiosResponse = transaction(database) {
        val offset = (pagina - 1) * limite

        val queryBase = (Beneficios innerJoin Socios).selectAll()

        filtro?.takeIf { it.isNotBlank() }?.let {
            val filtroLike = "%${it.lowercase()}%"
            queryBase.andWhere {
                (Socios.dni.lowerCase() like filtroLike) or
                        (Socios.nombre.lowerCase() like filtroLike) or
                        (Socios.apellido.lowerCase() like filtroLike)
            }
        }

        val total = queryBase.count()

        val beneficios = queryBase
            .orderBy(Beneficios.fechaOtorgado to SortOrder.DESC)
            .limit(n = limite, offset = offset.toLong())
            .map {
                BeneficioConSocioDTO(
                    beneficiosId = it[Beneficios.beneficiosId],
                    socioId = it[Socios.socioId],
                    nombre = it[Socios.nombre],
                    apellido = it[Socios.apellido],
                    dni = it[Socios.dni],
                    fechaOtorgado = it[Beneficios.fechaOtorgado]
                )
            }

        PaginadoBeneficiosResponse(beneficios = beneficios, total = total)
    }
}

