// src/main/kotlin/com/example/infraestructure/http/routes/ViajesPagosRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.command.ViajesPagos.CreateViajesPagosCommand
import com.example.application.commandhandler.ViajesPagos.ActualizarViajePagoHandler
import com.example.application.commandhandler.ViajesPagos.ViajePagoCommandHandler
import com.example.domain.entities.ViajePago
import com.example.infraestructure.persistence.ViajesPagosRepository
import com.example.infraestructure.persistence.connectToMySql
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Route.viajesPagosRoutes() {
    val database: Database = application.connectToMySql()
        ?: error("Error connecting to MySQL database")

    val viajesPagosRepository = ViajesPagosRepository(database)
    val viajesPagosHandler = ViajePagoCommandHandler(viajesPagosRepository)
    val actualizarViajePagoHandler = ActualizarViajePagoHandler(viajesPagosRepository)

    // Crear pago de viaje
    post("/viajesPagos") {
        try {
            val command = call.receive<CreateViajesPagosCommand>()
            println("Datos recibidos: $command")

            command.validate()               // si tu comando tiene validate()
            viajesPagosHandler.handle(command)

            call.respond(HttpStatusCode.Created, "El pago del viaje se ha registrado correctamente")
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, "Error: ${e.message}")
        } catch (e: Exception) {
            println("Error /viajesPagos: ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, "Error en el servidor")
        }
    }

    // Listar pagos por viajeId
    get("/viajePagos/{viajeId}") {
        try {
            val viajeId = call.parameters["viajeId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "viajeId inválido")

            val pagos = viajesPagosRepository.findByViajeId(viajeId)
            call.respond(pagos)
        } catch (e: Exception) {
            println("❌ Error al obtener pagos por viajeId: ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, "Error al obtener pagos")
        }
    }

    // Pagos (detalle completo) con paginación
    get("/viajePagosFull") {
        try {
            val viajeId = call.request.queryParameters["viajeId"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Falta viajeId")
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10

            val resultado = viajesPagosRepository.findByViajeIdConPaginacionFull(viajeId, page, pageSize)
            call.respond(resultado)
        } catch (e: Exception) {
            println("❌ Error /viajePagosFull: ${e.message}")
            call.respond(HttpStatusCode.InternalServerError, "Error al obtener pagos")
        }
    }

    // Eliminar pago de viaje
    delete("/viajePagos/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID inválido"))

        val eliminado = viajesPagosRepository.eliminarPorId(id)
        if (eliminado) {
            call.respond(HttpStatusCode.OK, mapOf("message" to "Pasajero eliminado"))
        } else {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Pasajero no encontrado"))
        }
    }

    // Actualizar pago de viaje
    // Nota: cambié a "/viajePagos/{id}" para mantener consistencia en el path.
    // Si necesitás mantener "/viajepagos/{id}" por compatibilidad, duplicá la ruta.
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
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al actualizar ViajePago"))
        }
    }

    // (Opcional) Alias para compatibilidad con el path anterior en minúsculas
    // patch("/viajepagos/{id}") { ... mismo contenido que arriba ... }
}
