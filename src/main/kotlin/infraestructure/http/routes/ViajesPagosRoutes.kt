// src/main/kotlin/com/example/infraestructure/http/routes/ViajesPagosRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.command.ViajesPagos.CreateViajesPagosCommand
import com.example.application.commandhandler.ViajesPagos.ActualizarViajePagoHandler
import com.example.application.commandhandler.ViajesPagos.ViajePagoCommandHandler
import com.example.domain.entities.ViajePago
import com.example.infraestructure.persistence.ViajesPagosRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Rutas de pagos de viajes recibiendo el repositorio ya inicializado.
 * No abre conexiones ni usa connectToMySql.
 */
fun Route.viajesPagosRoutes(
    viajesPagosRepository: ViajesPagosRepository
) {
    val viajesPagosHandler = ViajePagoCommandHandler(viajesPagosRepository)
    val actualizarViajePagoHandler = ActualizarViajePagoHandler(viajesPagosRepository)

    // POST /viajesPagos  (crear pago)
    post("/viajesPagos") {
        try {
            val command = call.receive<CreateViajesPagosCommand>()
            // command.validate()  // descomentar si tu comando implementa validate()
            viajesPagosHandler.handle(command)
            call.respond(HttpStatusCode.Created, "El pago del viaje se ha registrado correctamente")
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
        } catch (e: Exception) {
            call.application.log.error("Error en POST /viajesPagos", e)
            call.respond(HttpStatusCode.InternalServerError, "Error en el servidor")
        }
    }

    // GET /viajePagos/{viajeId}  (listar pagos por viaje)
    get("/viajePagos/{viajeId}") {
        try {
            val viajeId = call.parameters["viajeId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "viajeId inválido")

            val pagos = viajesPagosRepository.findByViajeId(viajeId)
            call.respond(pagos)
        } catch (e: Exception) {
            call.application.log.error("Error en GET /viajePagos/{viajeId}", e)
            call.respond(HttpStatusCode.InternalServerError, "Error al obtener pagos")
        }
    }

    // GET /viajePagosFull?viejeId=&page=&pageSize=  (detalle paginado)
    get("/viajePagosFull") {
        try {
            val viajeId = call.request.queryParameters["viajeId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Falta viajeId")
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10

            val resultado = viajesPagosRepository.findByViajeIdConPaginacionFull(viajeId, page, pageSize)
            call.respond(resultado)
        } catch (e: Exception) {
            call.application.log.error("Error en GET /viajePagosFull", e)
            call.respond(HttpStatusCode.InternalServerError, "Error al obtener pagos")
        }
    }

    // DELETE /viajePagos/{id}  (eliminar pago)
    delete("/viajePagos/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

        try {
            val eliminado = viajesPagosRepository.eliminarPorId(id)
            if (eliminado) call.respond(HttpStatusCode.OK, mapOf("message" to "Pago eliminado"))
            else            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Pago no encontrado"))
        } catch (e: Exception) {
            call.application.log.error("Error en DELETE /viajePagos/$id", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al eliminar pago"))
        }
    }

    // PATCH /viajePagos/{id}  (actualizar pago)
    patch("/viajePagos/{id}") {
        try {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

            val viajePagoData = call.receive<ViajePago>()
            if (viajePagoData.viajePagoId != id) {
                return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID no coincide"))
            }

            val actualizado = actualizarViajePagoHandler.actualizarViajePago(viajePagoData)
            call.respond(HttpStatusCode.OK, actualizado)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        } catch (e: Exception) {
            call.application.log.error("Error en PATCH /viajePagos/{id}", e)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al actualizar ViajePago"))
        }
    }

    // (Opcional) Alias legacy:
    // patch("/viajepagos/{id}") { ... }
}
