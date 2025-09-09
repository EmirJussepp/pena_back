package com.example.repository

import com.example.domain.dto.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl // 👈 IMPORTANTE
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReportesRepository(
    private val database: Database
) {

    private inline fun <T> jdbcQuery(
        sql: String,
        crossinline bind: (PreparedStatement) -> Unit,
        crossinline mapRow: (ResultSet) -> T
    ): List<T> {
        val exposedConn = TransactionManager.current().connection
        val conn = (exposedConn as JdbcConnectionImpl).connection  // ✅ conexión JDBC real

        val out = mutableListOf<T>()
        conn.prepareStatement(sql).use { st ->
            bind(st)
            st.executeQuery().use { rs ->
                while (rs.next()) {
                    out += mapRow(rs)
                }
            }
        }
        return out
    }

    private inline fun <T> jdbcQueryOne(
        sql: String,
        crossinline bind: (PreparedStatement) -> Unit,
        crossinline mapRow: (ResultSet) -> T
    ): T? = jdbcQuery(sql, bind, mapRow).firstOrNull()

    fun obtenerReporteIngresos(
        anio: Int,
        mes: Int? = null,
        cobradorId: Int? = null,
        metodoPagoId: Int? = null
    ): ReporteIngresosDTO = transaction(database) {
        val (inicio, finExcl) = if (mes != null) {
            val first = LocalDate.of(anio, mes, 1)
            first.atStartOfDay() to first.plusMonths(1).atStartOfDay()
        } else {
            LocalDate.of(anio, 1, 1).atStartOfDay() to LocalDate.of(anio + 1, 1, 1).atStartOfDay()
        }

        val where = buildString {
            append(" WHERE p.fecha_pago >= ? AND p.fecha_pago < ? ")
            if (cobradorId != null)   append(" AND s.cobrador_id = ? ")
            if (metodoPagoId != null) append(" AND p.metodo_pago_id = ? ")
        }

        fun bindCommon(st: PreparedStatement) {
            var i = 1
            st.setTimestamp(i++, Timestamp.valueOf(inicio))
            st.setTimestamp(i++, Timestamp.valueOf(finExcl))
            if (cobradorId != null)   st.setInt(i++, cobradorId)
            if (metodoPagoId != null) st.setInt(i++, metodoPagoId)
        }

        // A) Totales globales
        val sqlTot = """
            SELECT COALESCE(SUM(p.monto),0) AS total, COUNT(*) AS pagos
            FROM pagos p
            JOIN socios s ON s.socio_id = p.socio_id
            $where
        """.trimIndent()

        val (totalGlobal, cantPagosGlobal) = jdbcQueryOne(sqlTot, ::bindCommon) { rs: ResultSet ->
            (rs.getBigDecimal("total") ?: BigDecimal.ZERO) to rs.getLong("pagos")
        } ?: (BigDecimal.ZERO to 0L)

        val anual = IngresoAnualDTO(anio, totalGlobal, cantPagosGlobal)

        // B) Mensual
        val sqlMes = """
            SELECT MONTH(p.fecha_pago) AS mes,
                   COALESCE(SUM(p.monto),0) AS total,
                   COUNT(*) AS pagos
            FROM pagos p
            JOIN socios s ON s.socio_id = p.socio_id
            $where
            GROUP BY MONTH(p.fecha_pago)
            ORDER BY mes
        """.trimIndent()

        val mapMes = jdbcQuery(sqlMes, ::bindCommon) { rs: ResultSet ->
            rs.getInt("mes") to ((rs.getBigDecimal("total") ?: BigDecimal.ZERO) to rs.getLong("pagos"))
        }.toMap()

        val mesesListado = if (mes != null) listOf(mes) else (1..12).toList()
        val mesesDto = mesesListado.map { m ->
            val (t, c) = mapMes[m] ?: (BigDecimal.ZERO to 0L)
            IngresoMensualDTO(mes = m, total = t, pagos = c)
        }

        // C) Por cobrador
        val sqlCob = """
            SELECT s.cobrador_id AS id, COALESCE(c.nombre,'Sin nombre') AS nombre,
                   COALESCE(SUM(p.monto),0) AS total, COUNT(*) AS pagos
            FROM pagos p
            JOIN socios s ON s.socio_id = p.socio_id
            LEFT JOIN cobradores c ON c.cobrador_id = s.cobrador_id
            $where
            GROUP BY s.cobrador_id, c.nombre
            ORDER BY total DESC
        """.trimIndent()

        val porCobrador = jdbcQuery(sqlCob, ::bindCommon) { rs: ResultSet ->
            IngresoCobradorDTO(
                cobradorId = rs.getInt("id"),
                nombre     = rs.getString("nombre") ?: "Sin nombre",
                total      = rs.getBigDecimal("total") ?: BigDecimal.ZERO,
                pagos      = rs.getLong("pagos")
            )
        }

        // D) Por método de pago  👈 corregido a tabla `metodo_pago` (singular)
        val sqlMet = """
            SELECT p.metodo_pago_id AS id, COALESCE(mp.nombre,'(s/def)') AS nombre,
                   COALESCE(SUM(p.monto),0) AS total, COUNT(*) AS pagos
            FROM pagos p
            JOIN socios s ON s.socio_id = p.socio_id
            LEFT JOIN metodo_pago mp ON mp.metodo_pago_id = p.metodo_pago_id
            $where
            GROUP BY p.metodo_pago_id, mp.nombre
            ORDER BY total DESC
        """.trimIndent()

        val porMetodo = mutableListOf<IngresoMetodoDTO>()
        val metodosOrden = mutableListOf<Pair<Int,String>>() // columnas de la matriz
        jdbcQuery(sqlMet, ::bindCommon) { rs: ResultSet ->
            val id  = rs.getInt("id")
            val nom = rs.getString("nombre") ?: "(s/def)"
            porMetodo += IngresoMetodoDTO(
                metodoPagoId = id,
                nombre = nom,
                total = rs.getBigDecimal("total") ?: BigDecimal.ZERO,
                pagos = rs.getLong("pagos")
            )
            metodosOrden += id to nom
        }

        // E) Cruce cobrador × método
        val sqlCruce = """
            SELECT s.cobrador_id AS cobId,
                   COALESCE(c.nombre,'Sin nombre') AS cobrador,
                   p.metodo_pago_id AS mpId,
                   COALESCE(SUM(p.monto),0) AS total
            FROM pagos p
            JOIN socios s ON s.socio_id = p.socio_id
            LEFT JOIN cobradores c ON c.cobrador_id = s.cobrador_id
            $where
            GROUP BY s.cobrador_id, c.nombre, p.metodo_pago_id
        """.trimIndent()

        val matriz = linkedMapOf<Int, MutableMap<Int, BigDecimal>>() // cobId -> (mpId -> total)
        val nombresCobr = mutableMapOf<Int, String>()
        jdbcQuery(sqlCruce, ::bindCommon) { rs: ResultSet ->
            val cobId = rs.getInt("cobId")
            val mpId  = rs.getInt("mpId")
            val totC  = rs.getBigDecimal("total") ?: BigDecimal.ZERO
            nombresCobr[cobId] = rs.getString("cobrador") ?: "Sin nombre"
            val row = matriz.getOrPut(cobId) { mutableMapOf() }
            row[mpId] = totC
        }

        val metodosIds  = metodosOrden.map { it.first }
        val headers     = metodosOrden.map { it.second }

        val filas = matriz.entries.map { (cobId, cols) ->
            val vals = metodosIds.map { mid -> cols[mid] ?: BigDecimal.ZERO }
            val totalFila = vals.fold(BigDecimal.ZERO, BigDecimal::add)
            MatrizFilaDTO(
                cobradorId = cobId,
                cobrador = (nombresCobr[cobId] ?: "Sin nombre"),
                totalesPorMetodo = vals,
                totalFila = totalFila
            )
        }.sortedByDescending { it.totalFila }

        val totalPorMetodo = metodosIds.indices.map { idx ->
            filas.fold(BigDecimal.ZERO) { acc, f -> acc + f.totalesPorMetodo[idx] }
        }
        val granTotal = totalPorMetodo.fold(BigDecimal.ZERO, BigDecimal::add)

        val dtoMatriz = MatrizCobradorMetodoDTO(
            metodos = headers,
            filas = filas,
            totalPorMetodo = totalPorMetodo,
            granTotal = granTotal
        )

        val df = DateTimeFormatter.ISO_LOCAL_DATE
        val promedio = if (cantPagosGlobal > 0)
            totalGlobal.divide(BigDecimal.valueOf(cantPagosGlobal), 2, RoundingMode.HALF_UP)
        else BigDecimal.ZERO

        ReporteIngresosDTO(
            desde = inicio.toLocalDate().format(df),
            hastaExcl = finExcl.toLocalDate().format(df),
            filtro = mapOf(
                "anio" to anio.toString(),
                "mes" to mes?.toString(),
                "cobradorId" to cobradorId?.toString(),
                "metodoPagoId" to metodoPagoId?.toString()
            ),
            total = totalGlobal,
            cantidadPagos = cantPagosGlobal,
            promedioPorPago = promedio,
            anual = anual,
            meses = mesesDto,
            porCobrador = porCobrador,
            porMetodo = porMetodo,
            cruceCobradorMetodo = dtoMatriz
        )
    }
}
