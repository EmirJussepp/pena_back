// src/main/kotlin/com/example/infraestructure/http/routes/ReportesRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.security.requirePerm
import com.example.repository.ReportesRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import java.sql.Timestamp
import java.time.LocalDate

fun Route.reportes(
    reportesRepository: ReportesRepository,
    database: Database
) {
    authenticate("auth-jwt") {

        // JSON
        get("/reportes/ingresos") {
            if (!requirePerm(call, "movimientos:ver")) return@get

            val anio = call.request.queryParameters["anio"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Falta 'anio'"))

            val mes = call.request.queryParameters["mes"]?.toIntOrNull()
            if (mes != null && mes !in 1..12) {
                return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Mes fuera de rango (1..12)"))
            }

            val cobradorId   = call.request.queryParameters["cobradorId"]?.toIntOrNull()
            val metodoPagoId = call.request.queryParameters["metodoPagoId"]?.toIntOrNull()

            try {
                val dto = reportesRepository.obtenerReporteIngresos(anio, mes, cobradorId, metodoPagoId)
                call.respond(HttpStatusCode.OK, dto)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al generar el reporte"))
            }
        }

        // CSV
        get("/reportes/ingresos.csv") {
            if (!requirePerm(call, "movimientos:ver")) return@get

            val anio = call.request.queryParameters["anio"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Falta 'anio'"))

            val mes = call.request.queryParameters["mes"]?.toIntOrNull()
            if (mes != null && mes !in 1..12) {
                return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Mes fuera de rango (1..12)"))
            }

            val cobradorId   = call.request.queryParameters["cobradorId"]?.toIntOrNull()
            val metodoPagoId = call.request.queryParameters["metodoPagoId"]?.toIntOrNull()

            try {
                val first   = if (mes != null) LocalDate.of(anio, mes, 1) else LocalDate.of(anio, 1, 1)
                val inicio  = first.atStartOfDay()
                val finExcl = (if (mes != null) first.plusMonths(1) else LocalDate.of(anio + 1, 1, 1)).atStartOfDay()

                val where = buildString {
                    append(" WHERE p.fecha_pago >= ? AND p.fecha_pago < ? ")
                    if (cobradorId != null)   append(" AND s.cobrador_id = ? ")
                    if (metodoPagoId != null) append(" AND p.metodo_pago_id = ? ")
                }

                val sql = """
                    SELECT p.pago_id, p.fecha_pago, p.monto,
                           s.socio_id, s.dni, s.nombre, s.apellido,
                           c.cobrador_id, c.nombre AS cobrador,
                           mp.metodo_pago_id, mp.nombre AS metodo
                    FROM pagos p
                    JOIN socios s ON s.socio_id = p.socio_id
                    LEFT JOIN cobradores   c  ON c.cobrador_id = s.cobrador_id
                    LEFT JOIN metodos_pago mp ON mp.metodo_pago_id = p.metodo_pago_id
                    $where
                    ORDER BY p.fecha_pago
                """.trimIndent()

                val csv = org.jetbrains.exposed.sql.transactions.transaction(database) {
                    val exposedConn = TransactionManager.current().connection
                    val conn = (exposedConn as JdbcConnectionImpl).connection

                    val sb = StringBuilder(
                        "pago_id,fecha_pago,monto,socio_id,dni,nombre,apellido,cobrador_id,cobrador,metodo_id,metodo\n"
                    )

                    conn.prepareStatement(sql).use { st ->
                        var i = 1
                        st.setTimestamp(i++, Timestamp.valueOf(inicio))
                        st.setTimestamp(i++, Timestamp.valueOf(finExcl))
                        if (cobradorId != null)   st.setInt(i++, cobradorId)
                        if (metodoPagoId != null) st.setInt(i++, metodoPagoId)

                        st.executeQuery().use { rs ->
                            while (rs.next()) {
                                val nombre   = (rs.getString("nombre") ?: "").replace(",", " ")
                                val apellido = (rs.getString("apellido") ?: "").replace(",", " ")
                                val cobrador = (rs.getString("cobrador") ?: "").replace(",", " ")
                                val metodo   = (rs.getString("metodo") ?: "").replace(",", " ")

                                sb.append(
                                    listOf(
                                        rs.getInt("pago_id"),
                                        rs.getTimestamp("fecha_pago").toLocalDateTime(),
                                        rs.getBigDecimal("monto"),
                                        rs.getInt("socio_id"),
                                        rs.getString("dni") ?: "",
                                        nombre,
                                        apellido,
                                        rs.getInt("cobrador_id"),
                                        cobrador,
                                        rs.getInt("metodo_pago_id"),
                                        metodo
                                    ).joinToString(",")
                                ).append("\n")
                            }
                        }
                    }
                    sb.toString()
                }

                call.response.headers.append(
                    HttpHeaders.ContentDisposition,
                    "attachment; filename=\"ingresos_${anio}${if (mes != null) "_$mes" else ""}.csv\""
                )

                // ContentType.Text.CSV puede no estar en todas las versiones → uso parse
                call.respondText(csv, ContentType.parse("text/csv"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al generar el CSV"))
            }
        }
    }
}
