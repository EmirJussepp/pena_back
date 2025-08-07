package com.example.infraestructure.persistence

import com.example.domain.dto.BeneficioConSocioDTO
import com.example.domain.dto.PaginadoBeneficiosResponse
import com.example.domain.contracts.IBeneficioRepository
import com.example.domain.entities.Beneficios
import com.example.domain.entities.Cuotas
import com.example.domain.entities.EstadoBeneficio
import com.example.domain.entities.Socios

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

// ✅ asegúrate que apunta al archivo correcto

class BeneficioRepository(
    private val database: Database,
    private val socioRepository: SocioRepository
): IBeneficioRepository {
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


    override fun actualizarBeneficioSiCorresponde(socioId: Int)= transaction(database) {
        val estado = obtenerEstadoBeneficio(socioId)

        println("Estado beneficio para socio $socioId: $estado")

        when {
            estado.cuotasPagadas >= 6 && estado.cuotasImpagasVencidas <= 2 && estado.socioActivo && !estado.tieneBeneficio -> {
                Beneficios.insert {
                    it[Beneficios.socioId] = socioId
                    it[Beneficios.fechaOtorgado] = LocalDateTime.now()
                }
                println("🟢 Beneficio otorgado a Socio ID: $socioId")
            }

            (estado.cuotasPagadas < 6 || estado.cuotasImpagasVencidas > 2 || !estado.socioActivo) && estado.tieneBeneficio -> {
                Beneficios.deleteWhere { Beneficios.socioId eq socioId }
                println("🔴 Beneficio revocado para Socio ID: $socioId")
            }

            else -> println("ℹ️ No se modificó el beneficio para Socio ID: $socioId")
        }
    }

    fun recalcularBeneficiosATodos(batchSize: Int = 100) {
        val total = transaction(database) { Socios.selectAll().count() }
        var offset = 0
        while (offset < total) {
            transaction(database) {
                val sociosBatch = Socios.selectAll()
                    .limit(batchSize, offset.toLong())
                    .map { it[Socios.socioId] }
                sociosBatch.forEach { socioId ->
                    actualizarBeneficioSiCorresponde(socioId) // dentro de la misma transacción
                }
            }
            offset += batchSize
        }
    }


    fun obtenerEstadoBeneficio(socioId: Int): EstadoBeneficio {
        val cuotasPagadas = Cuotas.select {
            (Cuotas.socioId eq socioId) and (Cuotas.estado eq true)
        }.count()

        val cuotasImpagasVencidas = Cuotas.select {
            (Cuotas.socioId eq socioId) and (Cuotas.estado eq false) and (Cuotas.fechaVencimiento less LocalDateTime.now())
        }.count()

        val socioActivo = Socios.select {
            (Socios.socioId eq socioId) and (Socios.estado eq true)
        }.any()

        val tieneBeneficio = Beneficios.select {
            Beneficios.socioId eq socioId
        }.any()

        return EstadoBeneficio(
            cuotasPagadas = cuotasPagadas.toInt(),
            cuotasImpagasVencidas = cuotasImpagasVencidas.toInt(),
            socioActivo = socioActivo,
            tieneBeneficio = tieneBeneficio
        )
    }



}

