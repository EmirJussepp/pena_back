package com.example.domain.entities


import java.time.LocalDateTime
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Beneficios : Table("beneficios") {
    val beneficiosId = integer("beneficios_id").autoIncrement()
    val socioId = integer("socio_id").references(Socios.socioId)
    val fechaOtorgado = datetime("fecha_otorgado")

    override val primaryKey = PrimaryKey(beneficiosId)
}


data class Beneficio(
    val beneficiosId: Int? = null,
    val socioId: Int,
    val fechaOtorgado: LocalDateTime = LocalDateTime.now()
)