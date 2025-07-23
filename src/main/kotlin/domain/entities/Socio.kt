package com.example.domain.entities

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime // Correct import for timestamp
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import java.math.BigDecimal



object Socios : Table("socios") {
    val socioId = integer("socio_id").autoIncrement()
    val nombre = varchar("nombre", 255)
    val alias = varchar("alias", 255).nullable()
    val apellido = varchar("apellido", 255)
    val email = varchar("email", 255)
    val dni = varchar("dni", 59)
    val numSocioBoca = integer("num_socio_boca").nullable()
    val telefono = varchar("telefono",10)
    val fechaInicio = datetime("fecha_inicio").defaultExpression(CurrentDateTime) // Now using the correct timestamp function

    // Definir las relaciones
    val cobradorId = integer("cobrador_id").references(Cobradores.cobradoresId)
    val tipoSocioPeñaId = integer("tipo_id_socioPeña").references(TiposSocioPeña.tipoSocioPeñaId)
    val tipoBocaId = integer("tipo_boca_id").references(TiposSocioBoca.tipoSocioBocaId)
    val userId = integer("user_id").references(Users.userId)
    val localidadId = integer("localidad_id").references(Localidades.localidadId)
    val estado = bool("estado")
    val fechaDeBaja = datetime("fecha_de_baja").nullable()
    val direccion = varchar("direccion", 100).nullable()

    override val primaryKey = PrimaryKey(socioId)
}
@Serializable
data class Socio(
    val socioId: Int? = null,
    val nombre: String,
    val alias: String?,
    val apellido: String,
    val email: String,
    val dni: String,
    val numSocioBoca: Int?,
    val telefono: String,
    @Contextual val fechaInicio: LocalDateTime,
    val cobradorId: Int,
    val tipoSocioPeñaId: Int,
    val tipoBocaId: Int,
    val userId: Int,
    val localidadId: Int,
    val estado: Boolean,
    @Contextual val fechaDeBaja: LocalDateTime? = null,
    val direccion: String?
) {
    companion object {
        fun create(
            nombre: String,
            alias: String?,
            apellido: String,
            email: String,
            dni: String,
            numSocioBoca: Int?,
            telefono: String,
            fechaInicio: LocalDateTime,
            cobradorId: Int,
            tipoSocioPeñaId: Int,
            tipoBocaId: Int,
            userId: Int,
            localidadId: Int,
            estado: Boolean,
            fechaDeBaja: LocalDateTime? = null,
            direccion: String?
        ): Socio {
            return Socio(
                socioId = null,
                nombre = nombre,
                alias = alias,
                apellido = apellido,
                email = email,
                dni = dni,
                numSocioBoca = numSocioBoca,
                telefono = telefono,
                fechaInicio = fechaInicio,
                cobradorId = cobradorId,
                tipoSocioPeñaId = tipoSocioPeñaId,
                tipoBocaId = tipoBocaId,
                userId = userId,
                localidadId = localidadId,
                estado = estado,
                fechaDeBaja = fechaDeBaja,
                direccion= direccion
            )
        }
    }

    fun calcularMontoCuota(
        getMontoPeña: (Int) -> Double?,
        getMontoBoca: (Int) -> Double?
    ): BigDecimal {
        val montoPeña = getMontoPeña(tipoSocioPeñaId) ?: 0.0
        val montoBoca = getMontoBoca(tipoBocaId) ?: 0.0
        return BigDecimal(montoPeña + montoBoca)
    }
}
