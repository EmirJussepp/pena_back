package com.example.domain.entities

import com.example.application.command.ViajesBombonera.ViajeBomboneraCommand
import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.math.BigDecimal


// Definimos la tabla ViajesBombonera en la base de datos
object ViajesBombonera : Table("viajes_bombonera") {
    val viajeBomboneraId = integer("viaje_id").autoIncrement() // ID autoincremental
    val fechaViaje = datetime("fecha_viaje")
    val destino = varchar("destino", 100)


    override val primaryKey = PrimaryKey(viajeBomboneraId)
}


@Serializable
data class ViajeBombonera(
    val viajeBomboneraId: Int? = null,
    @Serializable(with = ViajeBomboneraCommand.LocalDateTimeSerializer::class)
    val fechaViaje: LocalDateTime,
    val destino: String,
) {
    companion object {
        fun create(
            fechaViaje: LocalDateTime,
            destino: String,

        ): ViajeBombonera {
            return ViajeBombonera(
                viajeBomboneraId = null,
                fechaViaje = fechaViaje,
                destino = destino,
            )
        }
    }
}
