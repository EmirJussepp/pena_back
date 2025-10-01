// src/main/kotlin/com/example/infraestructure/http/routes/MetodoPagoRoutes.kt
package com.example.infraestructure.http.routes

import com.example.application.command.metodoPago.CreateMetodoPagoCommand
import com.example.application.commandhandler.metodoPago.CreateMetodoPagoHandler
import com.example.infraestructure.persistence.MetodoPagoRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Rutas de métodos de pago recibiendo el repositorio ya inicializado.
 * No abre conexiones ni usa connectToMySql.
 */
fun Route.metodoPagoRoutes(
    metodoPagoRepository: MetodoPagoRepository
) {
    val createMetodoPagoHandler = CreateMetodoPagoHandler(metodoPagoRepository)

    route("/metodos-pago") {

        // POST /metodos-pago
        post {
            try {
                val body = call.receive<CreateMetodoPagoCommand>()
                createMetodoPagoHandler.handle(body)
                call.respond(HttpStatusCode.Created, mapOf("message" to "Método de pago creado exitosamente"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Datos inválidos")))
            } catch (e: Exception) {
                call.application.log.error("Error creando método de pago", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error en el servidor"))
            }
        }

        // GET /metodos-pago
        get {
            try {
                val metodos = metodoPagoRepository.findAll()
                call.respond(HttpStatusCode.OK, metodos)
            } catch (e: Exception) {
                call.application.log.error("Error obteniendo métodos de pago", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Error al obtener métodos de pago"))
            }
        }
    }
}
