package com.example.domain.entities

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal

object Cuotas : Table("cuotas") {
    val cuotaId = integer("cuota_id").autoIncrement()
    val socioId = integer("socio_id").references(Socios.socioId)
    val monto = decimal("monto", 10,2)
    val estado = bool("estado").default(false) // false = no pagado, true = pagado
    val fechaEmision = datetime("fecha_pago").nullable()
    val fechaVencimiento = datetime("fecha_vencimiento")

    override val primaryKey = PrimaryKey(cuotaId)
}
@Serializable
data class Cuota(
    val cuotaId: Int? = null,
    val socioId: Int,
    @Contextual val monto: BigDecimal,
    val estado: Boolean = false,
    @Contextual val fechaEmision: kotlinx.datetime.LocalDateTime? = null,
    @Contextual val fechaVencimiento: kotlinx.datetime.LocalDateTime
) {
    companion object {
        fun create(
            socioId: Int,
            monto: BigDecimal,
            fechaEmision: kotlinx.datetime.LocalDateTime,
            fechaVencimiento: kotlinx.datetime.LocalDateTime
        ): Cuota {
            return Cuota(
                cuotaId = null,
                socioId = socioId,
                monto = monto,
                estado = false,
                fechaEmision = fechaEmision,
                fechaVencimiento = fechaVencimiento
            )
        }
    }

}

