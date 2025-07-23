package com.example.domain.entities

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal
import com.example.domain.entities.metodosPago
import kotlinx.datetime.toLocalDateTime

object Pagos : Table("pagos") {
    val pagoId = integer("pago_id").autoIncrement()
    val socioId = integer("socio_id").references(Socios.socioId)
    val cuotaId = integer("cuota_id").references(Cuotas.cuotaId)
    val fechaPago = datetime("fecha_pago")
    val monto = decimal("monto", 10, 2)
    val metodoPagoId = integer("metodo_pago_id").references(metodosPago.metodoPagoId) // necesitas definir esta tabla

    override val primaryKey = PrimaryKey(pagoId)
}

@Serializable
data class Pago(
    val pagoId: Int? = null,
    val socioId: Int,
    val cuotaId: Int,
    @Contextual val fechaPago: kotlinx.datetime.LocalDateTime,
    @Contextual val monto: BigDecimal,
    val metodoPagoId: Int
) {
    companion object {
        fun create(
            socioId: Int,
            cuotaId: Int,
            monto: BigDecimal,
            metodoPagoId: Int,
            fechaPago: kotlinx.datetime.LocalDateTime = kotlinx.datetime.Clock.System.now()
                .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        ): Pago {
            return Pago(
                pagoId = null,
                socioId = socioId,
                cuotaId = cuotaId,
                fechaPago = fechaPago,
                monto = monto,
                metodoPagoId = metodoPagoId
            )
        }
    }
}
