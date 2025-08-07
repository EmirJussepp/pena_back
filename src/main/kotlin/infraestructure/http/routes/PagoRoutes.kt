package com.example.infraestructure.http.routes

import com.example.application.command.pagos.CrearPagoCommand
import com.example.application.commandhandler.pagos.CrearPagoCommandHandler
import com.example.domain.contracts.IPagoRepository

//import com.example.infrastructure.repositories.CuotaRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.pagoRoutes(
    pagoRepository: IPagoRepository,
//    cuotaRepository: CuotaRepository,
    crearPagoCommandHandler: CrearPagoCommandHandler
) {
    routing {

        // Crear un pago
        post("/pagos") {
            try {
                val command = call.receive<CrearPagoCommand>()
                println("📥 Recibido CrearPagoCommand: $command")

                crearPagoCommandHandler.handle(command)

                println("✅ Pago creado correctamente")
                call.respond(HttpStatusCode.Created, mapOf("message" to "Pago creado exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            } catch (e: Exception) {
                println("❌ Error al crear pago: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }
        get("/pagos") {
            try {
                val pagos = pagoRepository.findAll()
                call.respond(pagos)
            } catch (e: Exception) {
                println("❌ Error al obtener pagos: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener pagos"))
            }
        }
    }
}
