// src/main/kotlin/com/example/routes/CuotaRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.Service.CuotaService
import com.example.application.command.Cuota.CrearCuotaCommand
import com.example.application.commandhandler.CreateCuotaCommandHandler
import com.example.domain.contracts.ICuotaRepository
import com.example.domain.contracts.ISocioRepository
import com.example.domain.entities.RespuestaCuotas
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal

fun Route.cuotaRoutes(
    cuotaRepository: ICuotaRepository,
    createCuotaHandler: CreateCuotaCommandHandler,
    cuotaService: CuotaService,
    socioRepository: ISocioRepository
) {
    route("/cuotas") {

        // POST /cuotas
        post {
            try {
                val command = call.receive<CrearCuotaCommand>()
                createCuotaHandler.handle(command)
                call.respond(HttpStatusCode.Created, mapOf("message" to "Cuota creada exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error creando cuota", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        // POST /cuotas/generar/automatico
        post("/generar/automatico") {
            try {
                cuotaService.generarCuotasMensuales()
                call.respond(HttpStatusCode.OK, mapOf("message" to "Cuotas generadas exitosamente para todos los socios"))
            } catch (e: Exception) {
                call.application.log.error("Error generando cuotas automáticas", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al generar las cuotas automáticas"))
            }
        }

        // GET /cuotas  (todas)
        get {
            try {
                val cuotas = cuotaRepository.findAll()
                call.respond(cuotas)
            } catch (e: Exception) {
                call.application.log.error("Error obteniendo cuotas", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener cuotas"))
            }
        }

        // ==== Rutas de pendientes primero para no ser “tapadas” por {socioId} ====

        // GET /cuotas/pendientes/{socioId}
        get("pendientes/{socioId}") {
            val socioId = call.parameters["socioId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "socioId inválido"))

            try {
                val pendientes = cuotaRepository.findPendientesPorSocio(socioId)
                val totalAdeudado = pendientes.fold(BigDecimal.ZERO) { acc, c -> acc + c.monto }.toString()

                val cuotas = pendientes.map { c ->
                    mapOf(
                        "cuotaId"          to c.cuotaId.toString(),
                        "socioId"          to c.socioId.toString(),
                        "monto"            to c.monto.toString(),
                        "fechaPago"        to (c.fechaEmision?.toString() ?: "No disponible"),
                        "fechaVencimiento" to c.fechaVencimiento.toString(),
                        "estado"           to c.estado.toString()
                    )
                }

                call.respond(RespuestaCuotas(cuotas, totalAdeudado, cuotas.size))
            } catch (e: Exception) {
                call.application.log.error("Error listando pendientes por socioId", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al buscar cuotas pendientes"))
            }
        }

        // GET /cuotas/pendientes?dni=...
        get("pendientes") {
            val dni = call.request.queryParameters["dni"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Parámetro 'dni' requerido"))

            try {
                val socio = socioRepository.findByDni(dni)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Socio con DNI $dni no encontrado"))

                val pendientes = cuotaRepository.findPendientesPorSocio(requireNotNull(socio.socioId))
                val totalAdeudado = pendientes.fold(BigDecimal.ZERO) { acc, c -> acc + c.monto }.toString()

                val cuotasFormateadas = pendientes.map { c ->
                    mapOf(
                        "cuotaId"          to c.cuotaId.toString(),
                        "socioId"          to c.socioId.toString(),
                        "monto"            to c.monto.toString(),
                        "fechaPago"        to (c.fechaEmision?.toString() ?: "No disponible"),
                        "fechaVencimiento" to c.fechaVencimiento.toString(),
                        "estado"           to c.estado.toString()
                    )
                }

                call.respond(
                    HttpStatusCode.OK,
                    RespuestaCuotas(
                        cuotasPendientes = cuotasFormateadas,
                        totalAdeudado = totalAdeudado,
                        cantidadCuotasPendientes = cuotasFormateadas.size
                    )
                )
            } catch (e: Exception) {
                call.application.log.error("Error listando pendientes por DNI", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al buscar cuotas pendientes"))
            }
        }

        // ==== Por último, GET /cuotas/{socioId} ====
        get("{socioId}") {
            val socioId = call.parameters["socioId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "socioId inválido"))

            try {
                val cuotas = cuotaRepository.findBySocioId(socioId)
                call.respond(cuotas)
            } catch (e: Exception) {
                call.application.log.error("Error buscando cuotas por socioId", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al buscar cuotas"))
            }
        }

        // PUT /cuotas/pagar/{cuotaId}
        put("pagar/{cuotaId}") {
            val cuotaId = call.parameters["cuotaId"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "cuotaId inválido"))

            try {
                val ok = cuotaRepository.marcarComoPagada(cuotaId)
                if (ok) call.respond(HttpStatusCode.OK, mapOf("message" to "Cuota pagada correctamente"))
                else     call.respond(HttpStatusCode.NotFound, mapOf("error" to "Cuota no encontrada"))
            } catch (e: Exception) {
                call.application.log.error("Error pagando cuota", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al pagar cuota"))
            }
        }

        // GET /cuotas/socio/{socioId}/tienecuota
        get("socio/{socioId}/tienecuota") {
            val socioId = call.parameters["socioId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "socioId inválido"))

            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val mesActual = now.monthNumber
                val anioActual = now.year

                val tieneCuota = cuotaRepository.existeCuotaEnMes(socioId, mesActual, anioActual)
                call.respond(mapOf("tieneCuota" to tieneCuota))
            } catch (e: Exception) {
                call.application.log.error("Error verificando cuota del mes", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al verificar cuota"))
            }
        }
    }

    // GET /cuotas-vencidas (fuera del grupo /cuotas)
    get("/cuotas-vencidas") {
        val cobradorId = call.request.queryParameters["cobradorId"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Falta el ID del cobrador")

        val mes      = call.request.queryParameters["mes"]?.toIntOrNull()
        val anio     = call.request.queryParameters["anio"]?.toIntOrNull()
        val dni      = call.request.queryParameters["dni"]
        val page     = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

        try {
            val items = cuotaRepository.obtenerCuotasVencidasPorCobrador(
                cobradorId = cobradorId,
                mes = mes,
                anio = anio,
                dni = dni,
                page = page,
                pageSize = pageSize
            )
            call.respond(HttpStatusCode.OK, items)
        } catch (e: Exception) {
            call.application.log.error("Error obteniendo cuotas vencidas", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener cuotas vencidas"))
        }
    }
}
