package com.example.routes

import com.example.application.Service.CuotaService
import com.example.application.command.Cuota.CrearCuotaCommand
import com.example.application.commandhandler.CreateCuotaCommandHandler
import com.example.domain.contracts.ICuotaRepository
import com.example.domain.contracts.ISocioRepository
import com.example.domain.entities.Cuota
import com.example.domain.entities.RespuestaCuotas
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.Database
import java.math.BigDecimal

fun Application.cuotaRoutes(
    cuotaRepository: ICuotaRepository,
    createCuotaHandler: CreateCuotaCommandHandler,
    cuotaService: CuotaService,
    socioRepository: ISocioRepository
) {
    routing {

        routing {

            post("/cuotas") {
                try {
                    val command = call.receive<CrearCuotaCommand>()
                    createCuotaHandler.handle(command)
                    call.respond(HttpStatusCode.Created, mapOf("message" to "Cuota creada exitosamente"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
                }
            }

            post("/cuotas/generar/automatico") {
                try {
                    cuotaService.generarCuotasMensuales()
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("message" to "Cuotas generadas exitosamente para todos los socios")
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al generar las cuotas automáticas")
                    )
                }
            }

            get("/cuotas") {
                try {
                    val cuotas = cuotaRepository.findAll()
                    call.respond(cuotas)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener cuotas"))
                }
            }

            get("/cuotas/{socioId}") {
                val socioId = call.parameters["socioId"]?.toIntOrNull()
                if (socioId == null) return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "socioId inválido")
                )

                try {
                    val cuotas = cuotaRepository.findBySocioId(socioId)
                    call.respond(cuotas)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al buscar cuotas"))
                }
            }

            get("/cuotas/pendientes/{socioId}") {
                val socioId = call.parameters["socioId"]?.toIntOrNull()
                if (socioId == null) return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "socioId inválido")
                )

                try {
                    val pendientes = cuotaRepository.findPendientesPorSocio(socioId)
                    val totalAdeudado = pendientes.fold(BigDecimal.ZERO) { acc, cuota -> acc + cuota.monto }.toString()

                    val cuotas = pendientes.map { cuota ->
                        mapOf(
                            "cuotaId" to cuota.cuotaId.toString(),
                            "socioId" to cuota.socioId.toString(),
                            "monto" to cuota.monto.toString(),
                            "fechaPago" to (cuota.fechaEmision?.toString() ?: "No disponible"),
                            "fechaVencimiento" to cuota.fechaVencimiento.toString(),
                            "estado" to cuota.estado.toString()
                        )
                    }

                    val respuesta = RespuestaCuotas(cuotas, totalAdeudado, cuotas.size)
                    call.respond(respuesta)

                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Error al buscar cuotas pendientes")
                    )
                }
            }
            get("/cuotas/pendientes") {
                val dni = call.request.queryParameters["dni"]

                if (dni.isNullOrBlank()) {
                    return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Parámetro 'dni' requerido"))
                }

                try {
                    val socio = socioRepository.findByDni(dni)

                    if (socio == null) {
                        return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Socio con DNI $dni no encontrado"))
                    }

                    val pendientes = cuotaRepository.findPendientesPorSocio(socio.socioId!!)

                    val totalAdeudado = pendientes.fold(BigDecimal.ZERO) { acc, cuota -> acc + cuota.monto }.toString()

                    val cuotasFormateadas = pendientes.map { cuota ->
                        mapOf(
                            "cuotaId" to cuota.cuotaId.toString(),
                            "socioId" to cuota.socioId.toString(),
                            "monto" to cuota.monto.toString(),
                            "fechaPago" to (cuota.fechaEmision?.toString() ?: "No disponible"),
                            "estado" to cuota.estado.toString()
                        )
                    }

                    val respuesta = RespuestaCuotas(
                        cuotasPendientes = cuotasFormateadas,
                        totalAdeudado = totalAdeudado,
                        cantidadCuotasPendientes = cuotasFormateadas.size
                    )

                    call.respond(HttpStatusCode.OK, respuesta)

                } catch (e: Exception) {
                    println("❌ Error al buscar cuotas pendientes por DNI: ${e.message}")
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al buscar cuotas pendientes"))
                }
            }


            put("/cuotas/pagar/{cuotaId}") {
                val cuotaId = call.parameters["cuotaId"]?.toIntOrNull()
                if (cuotaId == null) return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "cuotaId inválido")
                )

                try {
                    val result = cuotaRepository.marcarComoPagada(cuotaId)
                    if (result) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Cuota pagada correctamente"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Cuota no encontrada"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al pagar cuota"))
                }
            }

            get("/cuotas/socio/{socioId}/tienecuota") {
                val socioId = call.parameters["socioId"]?.toIntOrNull()
                if (socioId == null) return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "socioId inválido")
                )

                try {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val mesActual = now.monthNumber
                    val anioActual = now.year

                    val tieneCuota = cuotaRepository.existeCuotaEnMes(socioId, mesActual, anioActual)
                    call.respond(mapOf("tieneCuota" to tieneCuota))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al verificar cuota"))
                }
            }
            get("/cuotas-vencidas") {
                val cobradorId = call.request.queryParameters["cobradorId"]?.toIntOrNull()
                val mes        = call.request.queryParameters["mes"]?.toIntOrNull()
                val anio       = call.request.queryParameters["anio"]?.toIntOrNull()
                val dni        = call.request.queryParameters["dni"]
                val page       = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize   = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

                if (cobradorId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Falta el ID del cobrador")
                    return@get
                }

                val items = cuotaRepository.obtenerCuotasVencidasPorCobrador(
                    cobradorId = cobradorId,
                    mes = mes,
                    anio = anio,
                    dni = dni,
                    page = page,
                    pageSize = pageSize
                )

                call.respond(HttpStatusCode.OK, items)
            }



        }
    }
}








        // Obtener cuotas vencidas
//        get("/cuotas/vencidas/{socioId}") {
//            try {
//                val vencidas = cuotaRepository.findVencidas()
//                call.respond(vencidas)
//            } catch (e: Exception) {
//                println("❌ Error al obtener cuotas vencidas: ${e.message}")
//                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al buscar cuotas vencidas"))
//            }
//        }


